package org.rest.sec.persistence.setup;

import java.util.Set;

import org.rest.sec.model.Privilege;
import org.rest.sec.model.Role;
import org.rest.sec.model.User;
import org.rest.sec.persistence.service.IPrivilegeService;
import org.rest.sec.persistence.service.IRoleService;
import org.rest.sec.persistence.service.IUserService;
import org.rest.sec.util.SecurityConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

// @Profile( "production" )
@Component
public class SecuritySetup implements ApplicationListener< ContextRefreshedEvent >{
	static final Logger logger = LoggerFactory.getLogger( SecuritySetup.class );
	
	private boolean setupDone;
	
	@Autowired
	private IUserService userService;
	
	@Autowired
	private IRoleService roleService;
	
	@Autowired
	private IPrivilegeService privilegeService;
	
	public SecuritySetup(){
		super();
	}
	
	//
	
	/**
	 * - note that this is a compromise - the flag makes this bean statefull which can (and will) be avoided in the future by a more advanced mechanism <br>
	 * - the reason for this is that the context is refreshed more than once throughout the lifecycle of the deployable <br>
	 * - alternatives: proper persisted versioning
	 */
	@Override
	public final void onApplicationEvent( final ContextRefreshedEvent event ){
		if( !setupDone ){
			privilegeService.deleteAll();
			roleService.deleteAll();
			userService.deleteAll();
			
			createPrivileges();
			createRoles();
			createPrincipals();
			
			setupDone = true;
		}
	}
	
	// Privilege
	
	private void createPrivileges(){
		createPrivilegeIfNotExisting( SecurityConstants.PRIVILEGE_USER_WRITE );
		createPrivilegeIfNotExisting( SecurityConstants.PRIVILEGE_ROLE_WRITE );
	}
	final void createPrivilegeIfNotExisting( final String name ){
		final Privilege entityByName = privilegeService.findByName( name );
		if( entityByName == null ){
			final Privilege entity = new Privilege( name );
			privilegeService.create( entity );
		}
	}
	
	// Role
	
	private void createRoles(){
		final Privilege privilegeUserWrite = privilegeService.findByName( SecurityConstants.PRIVILEGE_USER_WRITE );
		final Privilege privilegeRoleWrite = privilegeService.findByName( SecurityConstants.PRIVILEGE_ROLE_WRITE );
		
		createRoleIfNotExisting( SecurityConstants.ROLE_ADMIN, Sets.<Privilege> newHashSet( privilegeUserWrite, privilegeRoleWrite ) );
	}
	final void createRoleIfNotExisting( final String name, final Set< Privilege > privileges ){
		final Role entityByName = roleService.findByName( name );
		if( entityByName == null ){
			final Role entity = new Role( name );
			entity.setPrivileges( privileges );
			roleService.create( entity );
		}
	}
	
	// Principal/User
	
	final void createPrincipals(){
		final Role roleAdmin = roleService.findByName( SecurityConstants.ROLE_ADMIN );
		
		createPrincipalIfNotExisting( SecurityConstants.ADMIN_USERNAME, SecurityConstants.ADMIN_PASSWORD, Sets.<Role> newHashSet( roleAdmin ) );
	}
	final void createPrincipalIfNotExisting( final String loginName, final String pass, final Set< Role > roles ){
		final User entityByName = userService.findByName( loginName );
		if( entityByName == null ){
			final User entity = buildUser( loginName, pass, roles );
			userService.create( entity );
		}
	}
	final User buildUser( final String name, final String pass, final Set< Role > roles ){
		final User user = new User( name, pass );
		user.setRoles( roles );
		return user;
	}
	
}