package com.campusguide.personal.ai.atlas.orchestration.communication;

import java.util.List;

/**
 * Interface representing a communication channel for an agent or topic.
 */
public interface AgentChannel {

    String getChannelId();

    void publish(AgentMessage message);

    List<AgentMessage> poll(int maxMessages);

    void subscribe(String agentId);

    void unsubscribe(String agentId);

    List<String> getSubscribers();
}
