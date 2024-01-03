package com.mcxccl.keycloakclient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class BugStatisticsScheduler {

	@Value("${bugtracker.api.url}")
	private String API_URL;

	private final RestClient apiClient = RestClient.create();
	@Autowired
	private OAuth2AuthorizedClientService azdCliService;

	/* used for scheduler or in service layer. no HTTP */
	@Autowired
	private AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientServiceOAuth2AuthorizedClientManager;

	/* midnight run */
	@Scheduled(fixedDelay = 5000)
	public void  dumpStatistics() {
		System.out.println("==> firing dumpStatistics()");

		// Build an OAuth2 request for the Okta provider
		OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest.withClientRegistrationId("keycloak-oidc")
				.principal("bugtracker-stats").build();

		// Perform the actual authorization request using the authorized client service
		// and authorized client
		// manager. This is where the JWT is retrieved from Keycloak
		OAuth2AuthorizedClient authorizedClient = this.authorizedClientServiceOAuth2AuthorizedClientManager.authorize(authorizeRequest);

		// Get the token from the authorized client object
		OAuth2AccessToken token = authorizedClient.getAccessToken();
		System.out.println("Token = " + token.getTokenValue());

		BugStatistics statistics = getBugStatistics(token.getTokenValue());
		System.out.printf("Open : %d, Closed : %d\n", statistics.numOpen(), statistics.numClosed());
	}

	public BugStatistics getBugStatistics(String token) {
		return apiClient.get().uri(API_URL + "/statistics").header("Authorization", "bearer " + token).retrieve()
				.body(BugStatistics.class);
	}

	private String getAccessToken() {
		var authn = (OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
		String authzdId = authn.getAuthorizedClientRegistrationId();
		String name = authn.getName();

		OAuth2AuthorizedClient authzdCli = azdCliService.loadAuthorizedClient(authzdId, name);
		OAuth2AccessToken token = authzdCli.getAccessToken();

		String tokenValue = token.getTokenValue();
		System.out.println("** TOKEN = " + tokenValue);

		return tokenValue;
	}
}