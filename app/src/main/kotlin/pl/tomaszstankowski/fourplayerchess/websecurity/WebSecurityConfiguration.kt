package pl.tomaszstankowski.fourplayerchess.websecurity

import org.springframework.context.annotation.Configuration
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
                .antMatchers("/token").permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .addFilter(JwtAuthenticationFilter(authenticationService, authenticationManager()))
                .exceptionHandling()
                .authenticationEntryPoint(BasicAuthenticationEntryPoint())
    }
}