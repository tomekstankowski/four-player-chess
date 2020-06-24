package pl.tomaszstankowski.fourplayerchess.websecurity

import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy.STATELESS
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint
import pl.tomaszstankowski.fourplayerchess.auth.AuthenticationService

@Configuration
class WebSecurityConfiguration(private val authenticationService: AuthenticationService)
    : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity?) {
        if (http == null) return
        http.csrf().disable()
                .formLogin().disable()
                .logout().disable()
                .httpBasic().disable()
                .sessionManagement().sessionCreationPolicy(STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/lobbies").permitAll()
                .antMatchers("/socket/**").permitAll()
                .antMatchers("/token").permitAll()
                .antMatchers(HttpMethod.OPTIONS).permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .addFilter(
                        WebAuthenticationFilter(
                                AuthenticationHelper(authenticationService),
                                authenticationManager()
                        )
                )
                .exceptionHandling()
                .authenticationEntryPoint(BasicAuthenticationEntryPoint())
    }
}