package dev.jlarsen.authclientdemo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Controller
public class ViewController {

    @Value("${resource-server.uri}")
    private URI serverUri;

    @GetMapping("/")
    public String index(Model model) {
        return "index";
    }

    @GetMapping(value = "/public")
    public String publicEndpoint(Model model) {
        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(serverUri + "/api/public", String.class);
        if (response != null) {
            model.addAttribute("response", response);
        } else {
            model.addAttribute("response", "Failed to receive any data from resource server..");
        }
        return "public";
    }
}
