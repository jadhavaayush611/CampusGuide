import { AtlasError } from './utils';

export class StreamingClient {
  constructor(atlasClient) {
    this.client = atlasClient;
    this.processedEventIds = new Set();
  }

  /**
   * Streams chat responses via Server-Sent Events (SSE).
   *
   * @param {Object} chatRequest - AtlasChatRequest payload
   * @param {Object} callbacks - Event callbacks for all 12 stream event types
   * @param {Object} options - Options including reconnect maxRetries, timeoutMs
   * @returns {Object} Streaming session controller with cancel() method
   */
  streamChat(chatRequest, callbacks = {}, options = {}) {
    const {
      onConnectionOpened = () => {},
      onThinking = () => {},
      onReasoning = () => {},
      onPlanning = () => {},
      onExecutionStarted = () => {},
      onToolStarted = () => {},
      onToolCompleted = () => {},
      onExecutionCompleted = () => {},
      onResponseToken = () => {},
      onCompletion = () => {},
      onError = () => {},
      onConnectionClosed = () => {},
      onEvent = () => {},
    } = callbacks;

    const maxRetries = options.maxRetries ?? 3;
    const timeoutMs = options.timeoutMs ?? 300000;
    let retryCount = 0;
    let lastEventId = options.lastEventId || null;
    let controller = new AbortController();
    let isCancelled = false;
    let timeoutTimer = null;

    const processEvent = (eventType, eventData, eventId) => {
      if (eventId) {
        if (this.processedEventIds.has(eventId)) {
          // Duplicate event prevention
          return;
        }
        this.processedEventIds.add(eventId);
        lastEventId = eventId;
        // Limit set size to prevent memory leak
        if (this.processedEventIds.size > 500) {
          const firstItem = this.processedEventIds.values().next().value;
          this.processedEventIds.delete(firstItem);
        }
      }

      onEvent({ type: eventType, data: eventData, id: eventId });

      switch (eventType) {
        case 'CONNECTION_OPENED':
          onConnectionOpened(eventData);
          break;
        case 'THINKING':
          onThinking(eventData);
          break;
        case 'REASONING':
          onReasoning(eventData);
          break;
        case 'PLANNING':
          onPlanning(eventData);
          break;
        case 'EXECUTION_STARTED':
          onExecutionStarted(eventData);
          break;
        case 'TOOL_STARTED':
          onToolStarted(eventData);
          break;
        case 'TOOL_COMPLETED':
          onToolCompleted(eventData);
          break;
        case 'EXECUTION_COMPLETED':
          onExecutionCompleted(eventData);
          break;
        case 'RESPONSE_TOKEN':
          onResponseToken(eventData);
          break;
        case 'COMPLETION':
          onCompletion(eventData);
          break;
        case 'ERROR':
          onError(eventData);
          break;
        case 'CONNECTION_CLOSED':
          onConnectionClosed(eventData);
          this.close();
          break;
        default:
          break;
      }
    };

    const startStream = async () => {
      if (isCancelled) return;

      try {
        const headers = await this.client.getHeaders();
        headers['Accept'] = 'text/event-stream';
        headers['Content-Type'] = 'application/json';
        if (lastEventId) {
          headers['Last-Event-ID'] = lastEventId;
        }

        const url = `${this.client.baseUrl}/api/v1/atlas/chat/stream`;
        controller = new AbortController();

        if (timeoutMs > 0) {
          timeoutTimer = setTimeout(() => {
            if (!isCancelled) {
              controller.abort();
              onError(new AtlasError('Stream request timed out', 504));
            }
          }, timeoutMs);
        }

        const response = await fetch(url, {
          method: 'POST',
          headers,
          body: JSON.stringify(chatRequest),
          signal: controller.signal,
        });

        if (!response.ok) {
          const errJson = await response.json().catch(() => ({}));
          throw AtlasError.fromResponse(response.status, errJson);
        }

        const reader = response.body.getReader();
        const decoder = new TextDecoder();
        let buffer = '';

        while (true) {
          const { done, value } = await reader.read();
          if (done) break;

          buffer += decoder.decode(value, { stream: true });
          const lines = buffer.split('\n');
          buffer = lines.pop(); // Keep unparsed tail

          let currentEvent = null;
          let currentId = null;
          let currentData = '';

          for (const line of lines) {
            if (line.startsWith('id:')) {
              currentId = line.substring(3).trim();
            } else if (line.startsWith('event:')) {
              currentEvent = line.substring(6).trim();
            } else if (line.startsWith('data:')) {
              currentData += line.substring(5).trim();
            } else if (line === '') {
              if (currentEvent || currentData) {
                let parsedData = currentData;
                try {
                  parsedData = JSON.parse(currentData);
                } catch {
                  // Keep as string if not JSON
                }
                const eventType = currentEvent || (parsedData && parsedData.type) || 'MESSAGE';
                processEvent(eventType, parsedData, currentId);
                currentEvent = null;
                currentId = null;
                currentData = '';
              }
            }
          }
        }
      } catch (err) {
        if (isCancelled || err.name === 'AbortError') return;

        if (retryCount < maxRetries) {
          retryCount++;
          const backoff = Math.pow(2, retryCount) * 500;
          setTimeout(() => startStream(), backoff);
        } else {
          onError(err instanceof AtlasError ? err : new AtlasError(err.message, 500));
        }
      } finally {
        if (timeoutTimer) clearTimeout(timeoutTimer);
      }
    };

    startStream();

    return {
      cancel: () => {
        isCancelled = true;
        if (timeoutTimer) clearTimeout(timeoutTimer);
        controller.abort();
        onConnectionClosed({ status: 'cancelled' });
      },
    };
  }
}
