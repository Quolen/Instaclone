package com.example.instaclone.controllers;

import com.example.instaclone.dto.ChatNameDTO;
import com.example.instaclone.entity.Chat;
import com.example.instaclone.entity.Message;
import com.example.instaclone.security.JWTTokenProvider;
import com.example.instaclone.services.ChatService;
import com.example.instaclone.services.MessageService;
import com.example.instaclone.web.ChatController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@EnableWebSocketMessageBroker
class ChatControllerTest {

    private final MockMvc mockMvc;
    private final ChatController chatController;

    @MockBean
    private MessageService messageService;

    @MockBean
    private ChatService chatService;

    @MockBean
    private SimpMessagingTemplate simpMessagingTemplate;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JWTTokenProvider jwtTokenProvider;

    @Autowired
    public ChatControllerTest(MockMvc mockMvc, ChatController chatController) {
        this.mockMvc = mockMvc;
        this.chatController = chatController;
    }

    private String jwtToken;
    private WebSocketStompClient stompClient;
    private BlockingQueue<Message> blockingQueue;

    @LocalServerPort
    private int port;

    private final WebSocketHttpHeaders headers = new WebSocketHttpHeaders();

    @BeforeEach
    public void setUp() {
        jwtToken = "mock-jwt-token";

        UserDetails userDetails = Mockito.mock(UserDetails.class);
        given(userDetails.getUsername()).willReturn("testUser");

        Authentication authentication = Mockito.mock(Authentication.class);
        given(authentication.getPrincipal()).willReturn(userDetails);
        given(authentication.isAuthenticated()).willReturn(true);

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        given(securityContext.getAuthentication()).willReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        given(authenticationManager.authenticate(any(Authentication.class))).willReturn(authentication);

        WebSocketClient webSocketClient = new StandardWebSocketClient();
        List<Transport> transports = new ArrayList<>(
                List.of(new WebSocketTransport(webSocketClient))
        );
        SockJsClient sockJsClient = new SockJsClient(transports);
        stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        blockingQueue = new LinkedBlockingQueue<>();
    }

    class DefaultStompFrameHandler implements StompFrameHandler {
        @Override
        public Type getPayloadType(StompHeaders headers) {
            return Message.class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            System.out.println("Headers: " + headers.toString());
            System.out.println("Payload type: " + payload.getClass().getName());
            blockingQueue.add((Message) payload);
            System.out.println("Received frame in DefaultStompFrameHandler: " + payload);
        }
    }

    @Test
    public void testGetMessages() throws Exception {
        List<Message> messageList = new ArrayList<>();
        Message message = new Message();
        message.setSender("testUser");
        message.setContent("Hello");
        messageList.add(message);

        Chat chat = new Chat();
        chat.setName("testChat");

        when(chatService.findChatByName(anyString())).thenReturn(chat);
        when(messageService.findAllByChat(any(Chat.class))).thenReturn(messageList);

        ChatNameDTO chatNameDTO = new ChatNameDTO();
        chatNameDTO.setChat("testChat");

        mockMvc.perform(post("/getMessages")
                        .header("Authorization", "Bearer " + jwtToken)  // Add JWT token
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(chatNameDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sender").value("testUser"))
                .andExpect(jsonPath("$[0].content").value("Hello"));
    }

    @Test
    public void testGetChats() throws Exception {
        List<Chat> chatList = new ArrayList<>();
        Chat chat = new Chat();
        chat.setName("testChat");
        chatList.add(chat);

        when(chatService.findByParticipant(anyString())).thenReturn(chatList);

        mockMvc.perform(post("/getChats")
                        .header("Authorization", "Bearer " + jwtToken)  // Add your JWT token here
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString("testUser")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("testChat"));
    }
}
