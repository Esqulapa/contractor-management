package pl.jarekzegzula.security;

import static pl.jarekzegzula.system.Constants.*;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

  @Value("${api.endpoint.base-url}")
  private String baseUrl;

  private final RSAPublicKey publicKey;

  private final RSAPrivateKey privateKey;

  private final CustomBasicAuthenticationEntryPoint customBasicAuthenticationEntryPoint;

  private final CustomBearerTokenAuthenticationEntryPoint customBearerTokenAuthenticationEntryPoint;

  private final CustomBearerTokenAccessDeniedHandler customBearerTokenAccessDeniedHandler;

  public SecurityConfiguration(
      CustomBasicAuthenticationEntryPoint customBasicAuthenticationEntryPoint,
      CustomBearerTokenAuthenticationEntryPoint customBearerTokenAuthenticationEntryPoint,
      CustomBearerTokenAccessDeniedHandler customBearerTokenAccessDeniedHandler)
      throws NoSuchAlgorithmException {
    this.customBasicAuthenticationEntryPoint = customBasicAuthenticationEntryPoint;
    this.customBearerTokenAuthenticationEntryPoint = customBearerTokenAuthenticationEntryPoint;
    this.customBearerTokenAccessDeniedHandler = customBearerTokenAccessDeniedHandler;

    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEYPAIR_ALGORITHM);
    keyPairGenerator.initialize(KEYPAIR_KEY_SIZE);
    KeyPair keyPair = keyPairGenerator.generateKeyPair();
    this.publicKey = (RSAPublicKey) keyPair.getPublic();
    this.privateKey = (RSAPrivateKey) keyPair.getPrivate();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http.authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers(HttpMethod.POST, this.baseUrl + "/users/token/refresh").permitAll()
                        .requestMatchers(HttpMethod.DELETE, this.baseUrl + "/users/*").hasRole("admin")
                    .anyRequest()
                    .authenticated())
        .csrf(AbstractHttpConfigurer::disable)
        .cors(Customizer.withDefaults())
        .httpBasic(
            httpBasic ->
                httpBasic.authenticationEntryPoint(this.customBasicAuthenticationEntryPoint))
        .oauth2ResourceServer(
            oauth2 ->
                oauth2
                    .jwt(Customizer.withDefaults())
                    .authenticationEntryPoint(this.customBearerTokenAuthenticationEntryPoint)
                    .accessDeniedHandler(this.customBearerTokenAccessDeniedHandler))
        .sessionManagement(
            sessionManagement ->
                sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(BCRYPT_PASSWORD_STRENGTH);
  }

  @Bean
  public JwtEncoder jwtEncoder() {
    JWK jwk = new RSAKey.Builder(this.publicKey).privateKey(privateKey).build();
    JWKSource<SecurityContext> jwkSet = new ImmutableJWKSet<>(new JWKSet(jwk));
    return new NimbusJwtEncoder(jwkSet);
  }

  @Bean
  public JwtDecoder jwtDecoder() {
    return NimbusJwtDecoder.withPublicKey(this.publicKey).build();
  }

  @Bean
  public JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter =
        new JwtGrantedAuthoritiesConverter();

    jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("authorities");

    jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");

    JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();

    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);

    return jwtAuthenticationConverter;
  }
}
