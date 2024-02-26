package kernel.jdon.moduleapi.global.config.auth;

import static kernel.jdon.auth.util.AesUtil.*;
import static kernel.jdon.auth.util.HmacUtil.*;
import static kernel.jdon.modulecommon.util.StringUtil.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kernel.jdon.auth.dto.JdonOAuth2User;
import kernel.jdon.auth.util.RedirectUrlUtil;
import kernel.jdon.member.domain.MemberRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JdonOAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {
	private final RedirectUrlUtil redirectUrlUtil;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException {
		JdonOAuth2User jdonOAuth2User = (JdonOAuth2User)authentication.getPrincipal();
		if (isTemporaryUser(jdonOAuth2User)) {
			String query = createUserInfoString(jdonOAuth2User.getEmail(), jdonOAuth2User.getSocialProviderType());
			String encodedQueryString = createEncryptQueryString(query);
			response.sendRedirect(joinToString(redirectUrlUtil.getLoginSuccessGuest(request.getHeader("Referer")),
				encodedQueryString));
			return;
		}
		response.sendRedirect(redirectUrlUtil.getLoginSuccessMember(request.getHeader("Referer")));
	}

	private boolean isTemporaryUser(JdonOAuth2User jdonOAuth2User) {
		return jdonOAuth2User.getAuthorities().contains(new SimpleGrantedAuthority(MemberRole.ROLE_GUEST.name()));
	}

	private String createUserInfoString(String email, String provider) {
		return joinToString(createQueryString("email", email), createQueryString("provider", provider));
	}

	private String createEncryptQueryString(String info) {
		String encoded = null;
		try {
			encoded = encryptAESCBC(info);
			encoded = joinToString(createQueryString("value", URLEncoder.encode(encoded, StandardCharsets.UTF_8)),
				createQueryString("hmac", URLEncoder.encode(generateHMAC(encoded), StandardCharsets.UTF_8)));
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
		return encoded;
	}
}