package dev.jlarsen.authclientdemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestController
public class WebController {

    @Value("${resource-server.uri}")
    private URI serverUri;

    @Autowired
    WebClient webClient;

    @Autowired
    OAuth2AuthorizedClientService oAuth2AuthorizedClientService;

    // todo - implement user interface
//    @GetMapping("/")
//    public String index(Model model,
//                        @RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient,
//                        @AuthenticationPrincipal OAuth2User oauth2User) {
//        model.addAttribute("userName", oauth2User.getName());
//        model.addAttribute("clientName", authorizedClient.getClientRegistration().getClientName());
//        model.addAttribute("userAttributes", oauth2User.getAttributes());
//        return "index";
//    }

    @GetMapping(value = "/public")
    public String publicEndpoint() {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(serverUri + "/api/public", String.class);
    }

    @GetMapping(value = "/private")
    public Map<String, Object> privateEndpoint(Authentication authentication) {

        Mono<String> response = webClient.get()
                .uri(serverUri + "/api/private")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class);

        OAuth2AuthorizedClient authorizedClient =
                oAuth2AuthorizedClientService.loadAuthorizedClient("auth-client",
                        authentication.getName());

        Map<String, Object> map = new HashMap<>();
        map.put("accessToken", authorizedClient.getAccessToken());
        map.put("refreshToken", authorizedClient.getRefreshToken());
        map.put("clientRegistration", authorizedClient.getClientRegistration());
        map.put("serverResponse", response.block());
        return map;
    }

    @PreAuthorize("hasAuthority('SCOPE_read:transactions')")
    @GetMapping(value = "/transactions")
    public Mono<Object> transactionsEndpoint(Authentication authentication) {

        return webClient.get()
                .uri(serverUri + "/api/transactions")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Object.class);
    }
}