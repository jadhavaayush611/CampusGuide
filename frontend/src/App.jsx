import React, { useState, useEffect } from 'react';
import { AtlasClient } from './sdk';
import './App.css';

const client = new AtlasClient({
  baseUrl: '',
  getToken: () => 'mock-jwt-token',
});

function App() {
  const [conversations, setConversations] = useState([]);
  const [activeConv, setActiveConv] = useState(null);
  const [messages, setMessages] = useState([]);
  const [events, setEvents] = useState([]);
  const [prompt, setPrompt] = useState('');
  const [isStreaming, setIsStreaming] = useState(false);
  const [currentResponse, setCurrentResponse] = useState('');

  useEffect(() => {
    loadConversations();
  }, []);

  const loadConversations = async () => {
    try {
      const list = await client.conversations.list();
      setConversations(list);
      if (list.length > 0 && !activeConv) {
        selectConversation(list[0]);
      }
    } catch (err) {
      console.warn('Backend offline or simulated mode:', err.message);
      // Fallback initial list
      const fallbackList = [
        { id: 'conv-demo-1', title: 'Fall 2026 Academic Advising', status: 'ACTIVE', type: 'ACADEMIC' },
        { id: 'conv-demo-2', title: 'Campus Navigation & Events', status: 'ACTIVE', type: 'CAMPUS' }
      ];
      setConversations(fallbackList);
      if (!activeConv) selectConversation(fallbackList[0]);
    }
  };

  const selectConversation = async (conv) => {
    setActiveConv(conv);
    try {
      const history = await client.conversations.getHistory(conv.id);
      setMessages(history.messages || []);
    } catch (err) {
      setMessages([
        { role: 'user', content: 'What courses should I take next semester?' },
        { role: 'assistant', content: 'I recommend Data Structures (CS201) and Linear Algebra (MATH204).' }
      ]);
    }
  };

  const handleCreateConversation = async () => {
    const title = prompt('Enter conversation title:', 'New Conversation');
    if (!title) return;
    try {
      const newConv = await client.conversations.create({ title, type: 'GENERAL' });
      setConversations([newConv, ...conversations]);
      selectConversation(newConv);
    } catch (err) {
      const mockConv = { id: `conv-${Date.now()}`, title, status: 'ACTIVE', type: 'GENERAL' };
      setConversations([mockConv, ...conversations]);
      selectConversation(mockConv);
    }
  };

  const handleArchive = async () => {
    if (!activeConv) return;
    try {
      const updated = await client.conversations.archive(activeConv.id);
      setActiveConv(updated);
      loadConversations();
    } catch (err) {
      setActiveConv({ ...activeConv, status: 'ARCHIVED' });
    }
  };

  const handleRestore = async () => {
    if (!activeConv) return;
    try {
      const updated = await client.conversations.restore(activeConv.id);
      setActiveConv(updated);
      loadConversations();
    } catch (err) {
      setActiveConv({ ...activeConv, status: 'ACTIVE' });
    }
  };

  const handleSendStream = async (e) => {
    e.preventDefault();
    if (!prompt.trim() || isStreaming) return;

    const userMsg = { role: 'user', content: prompt };
    setMessages((prev) => [...prev, userMsg]);
    const currentPrompt = prompt;
    setPrompt('');
    setIsStreaming(true);
    setEvents([]);
    setCurrentResponse('');

    let streamingContent = '';

    client.streaming.streamChat(
      {
        conversationId: activeConv?.id,
        prompt: currentPrompt,
        model: 'gpt-4o-mini',
      },
      {
        onEvent: (evt) => {
          setEvents((prev) => [...prev, evt]);
        },
        onResponseToken: (data) => {
          if (data && data.token) {
            streamingContent += data.token;
            setCurrentResponse(streamingContent);
          }
        },
        onCompletion: (data) => {
          setMessages((prev) => [
            ...prev,
            { role: 'assistant', content: streamingContent || 'Stream complete.' },
          ]);
          setCurrentResponse('');
          setIsStreaming(false);
        },
        onError: (err) => {
          console.error('Streaming error:', err);
          setIsStreaming(false);
        },
        onConnectionClosed: () => {
          setIsStreaming(false);
        },
      }
    );
  };

  return (
    <div className="app-container">
      <header className="app-header">
        <div className="app-title">
          <h1>Atlas AI Platform</h1>
          <span className="badge">Phase 3.7 — Batch 3.7.2</span>
        </div>
        <div style={{ display: 'flex', gap: '0.5rem' }}>
          <button className="btn btn-secondary" onClick={loadConversations}>Refresh</button>
          <button className="btn" onClick={handleCreateConversation}>+ New Chat</button>
        </div>
      </header>

      <div className="main-layout">
        {/* Left Panel: Conversations */}
        <aside className="panel">
          <div className="panel-header">
            <span>Conversations</span>
            <span style={{ fontSize: '0.8rem', color: '#9ca3af' }}>{conversations.length}</span>
          </div>
          <div className="conversation-list">
            {conversations.map((conv) => (
              <div
                key={conv.id}
                className={`conversation-item ${activeConv?.id === conv.id ? 'active' : ''}`}
                onClick={() => selectConversation(conv)}
              >
                <div className="conversation-title">{conv.title || 'Untitled Chat'}</div>
                <div className="conversation-meta">
                  <span>{conv.type || 'GENERAL'}</span>
                  <span>{conv.status}</span>
                </div>
              </div>
            ))}
          </div>
        </aside>

        {/* Center Panel: Active Chat & Streaming */}
        <main className="panel chat-area">
          <div className="panel-header">
            <div>
              <span>{activeConv?.title || 'Select a conversation'}</span>
              {activeConv && <span className="badge" style={{ marginLeft: '0.5rem' }}>{activeConv.status}</span>}
            </div>
            {activeConv && (
              <div style={{ display: 'flex', gap: '0.4rem' }}>
                {activeConv.status === 'ACTIVE' ? (
                  <button className="btn btn-secondary" style={{ padding: '0.3rem 0.6rem', fontSize: '0.8rem' }} onClick={handleArchive}>Archive</button>
                ) : (
                  <button className="btn btn-secondary" style={{ padding: '0.3rem 0.6rem', fontSize: '0.8rem' }} onClick={handleRestore}>Restore</button>
                )}
              </div>
            )}
          </div>

          <div className="chat-history">
            {messages.map((msg, idx) => (
              <div key={idx} className={`message-bubble ${msg.role === 'user' ? 'message-user' : 'message-assistant'}`}>
                {msg.content}
              </div>
            ))}
            {currentResponse && (
              <div className="message-bubble message-assistant">
                {currentResponse} <span className="badge" style={{ background: '#3b82f6', color: '#fff' }}>Streaming...</span>
              </div>
            )}
          </div>

          <form onSubmit={handleSendStream} className="chat-input-box">
            <input
              type="text"
              className="input-field"
              placeholder="Ask Atlas anything..."
              value={prompt}
              onChange={(e) => setPrompt(e.target.value)}
              disabled={isStreaming}
            />
            <button type="submit" className="btn" disabled={isStreaming || !prompt.trim()}>
              {isStreaming ? 'Streaming...' : 'Send SSE'}
            </button>
          </form>
        </main>

        {/* Right Panel: Streaming Event Timeline */}
        <aside className="panel">
          <div className="panel-header">
            <span>Stream Event Timeline</span>
            <span className="badge" style={{ background: isStreaming ? 'rgba(16,185,129,0.2)' : 'rgba(156,163,175,0.2)', color: isStreaming ? '#10b981' : '#9ca3af' }}>
              {isStreaming ? 'LIVE' : 'IDLE'}
            </span>
          </div>

          <div className="event-timeline">
            {events.length === 0 ? (
              <div style={{ color: '#9ca3af', padding: '1rem', textAlign: 'center' }}>
                No stream events received yet. Send a message to inspect the 12 SSE event phases.
              </div>
            ) : (
              events.map((evt, idx) => (
                <div key={idx} className={`event-card ${evt.type}`}>
                  <div className="event-header">
                    <span>{evt.type}</span>
                    <span style={{ color: '#9ca3af' }}>#{evt.data?.sequence || idx + 1}</span>
                  </div>
                  <div style={{ color: '#d1d5db', wordBreak: 'break-word' }}>
                    {typeof evt.data === 'object' ? JSON.stringify(evt.data) : String(evt.data)}
                  </div>
                </div>
              ))
            )}
          </div>
        </aside>
      </div>
    </div>
  );
}

export default App;
