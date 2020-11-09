package io.github.anantharajuc.sbat.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import io.github.anantharajuc.sbat.security.user.UserPrincipalService;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.TimeUnit;

/**
 * Application Security Configuration
 *
 * @author <a href="mailto:arcswdev@gmail.com">Anantha Raju C</a>
 *
 */
@Log4j2
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled=true)
public class ApplicationSecurityConfiguration extends WebSecurityConfigurerAdapter
{
	private final PasswordEncoder passwordEncoder;
	
	@Autowired
	public ApplicationSecurityConfiguration(PasswordEncoder passwordEncoder)
	{
		this.passwordEncoder = passwordEncoder;
	}

	/** Public URLs. */
    private static final String[] PUBLIC_MATCHERS = 
    {
            "/webjars/**",
            "/css/**",
            "/js/**",
            "/images/**",
            "/sbat/index/**",
            "/sbat/error/**",
            "/lang",
            "/h2-console/**"
    };	
	
	@Override
	protected void configure(HttpSecurity http) throws Exception
	{
		http
		.csrf()
			.disable()
		.authorizeRequests()		
			.antMatchers(PUBLIC_MATCHERS).permitAll()					
			.anyRequest().authenticated()
		.and()
			.formLogin()
				.loginPage("/sbat/login")
				.defaultSuccessUrl("/sbat/index")
				.failureUrl("/sbat/error")
				.permitAll()
				.passwordParameter("sbat-password")
				.usernameParameter("sbat-username") 
		.and()
			.rememberMe() 													
				.tokenValiditySeconds((int) TimeUnit.DAYS.toSeconds(21))
				.key("some-strong-key") 
				.rememberMeParameter("remember-me") 
	    .and()
	    	.httpBasic()
		.and()
			.logout()
				.logoutUrl("/logout")
				.clearAuthentication(true)
				.invalidateHttpSession(true)
				.deleteCookies("JSESSIONID","remember-me")
				.logoutSuccessUrl("/sbat/index") 
				.permitAll()
		.and()
			.exceptionHandling()
				.accessDeniedPage("/403");
		
		//https://stackoverflow.com/questions/53395200/h2-console-is-not-showing-in-browser
		http
			.headers()
			.frameOptions()
			.sameOrigin();
		
		http
			.sessionManagement()
			.maximumSessions(1)
			.sessionRegistry(sessionRegistry());
	}
	
	@Bean
    public SessionRegistry sessionRegistry() 
	{
        return new SessionRegistryImpl();
    }
	
	@Bean
    public ServletListenerRegistrationBean<HttpSessionEventPublisher> httpSessionEventPublisher() 
	{
        return new ServletListenerRegistrationBean<>(new HttpSessionEventPublisher());
    }
	
	@Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception 
	{
        auth.authenticationProvider(daoAuthenticationProvider(null));
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(UserPrincipalService userPrincipalService) 
    {
    	log.info("-----> DaoAuthenticationProvider : ");
    	
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        
        provider.setPasswordEncoder(passwordEncoder);
        provider.setUserDetailsService(userPrincipalService);
        
        return provider;
    }
}