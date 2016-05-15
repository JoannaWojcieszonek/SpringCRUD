# spring-security-ljug


for more information please look to official Spring Security documentation at link



## Architecture

#### Core Components
In Spring Security 3.0, the contents of the spring-security-core jar were stripped down to the bare minimum. It no longer contains any code related to web-application security, LDAP or namespace configuration. We’ll take a look here at some of the Java types that you’ll find in the core module. They represent the building blocks of the framework, so if you ever need to go beyond a simple namespace configuration then it’s important that you understand what they are, even if you don’t actually need to interact with them directly.

##### SecurityContextHolder, SecurityContext and Authentication Objects
The most fundamental object is SecurityContextHolder. This is where we store details of the present security context of the application, which includes details of the principal currently using the application. By default the SecurityContextHolder uses a ThreadLocal to store these details, which means that the security context is always available to methods in the same thread of execution, even if the security context is not explicitly passed around as an argument to those methods. 

Some applications aren’t entirely suitable for using a ThreadLocal, because of the specific way they work with threads. SecurityContextHolder can be configured with a strategy on startup to specify how you would like the context to be stored. All available strategies are SecurityContextHolder.MODE_GLOBAL, SecurityContextHolder.MODE_INHERITABLETHREADLOCAL, SecurityContextHolder.MODE_THREADLOCAL.

Inside the SecurityContextHolder we store details of the principal currently interacting with the application. Spring Security uses an Authentication object to represent this information. You won’t normally need to create an Authentication object yourself, but it is fairly common for users to query the Authentication object. You can use the following code block - from anywhere in your application - to obtain the name of the currently authenticated user, for example:

    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (principal instanceof UserDetails) {
        String username = ((UserDetails)principal).getUsername();
    } else {
        String username = principal.toString();
    }
    
The object returned by the call to getContext() is an instance of the SecurityContext interface. This is the object that is kept in thread-local storage. As we’ll see below, most authentication mechanisms withing Spring Security return an instance of UserDetails as the principal.

##### The UserDetailsService
UserDetails is a core interface in Spring Security. It represents a principal, but in an extensible and application-specific way. Think of UserDetails as the adapter between your own user database and what Spring Security needs inside the SecurityContextHolder. Being a representation of something from your own user database.

By now you’re probably wondering, so when do I provide a UserDetails object? How do I do that? I thought you said this thing was declarative and I didn’t need to write any Java code - what gives? The short answer is that there is a special interface called UserDetailsService. The only method on this interface accepts a String-based username argument and returns a UserDetails:

    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
    
This is the most common approach to loading information for a user within Spring Security and you will see it used throughout the framework whenever information on a user is required. According to method contract, when your application not found user in database must not return null, but must throw exception UsernameNotFoundException; On successful authentication, UserDetails is used to build the Authentication object that is stored in the SecurityContextHolder.

##### GrantedAuthority
Besides the principal, another important method provided by Authentication is getAuthorities(). This method provides an array of GrantedAuthority objects. A GrantedAuthority is, not surprisingly, an authority that is granted to the principal. Such authorities are usually "roles", such as ROLE_ADMINISTRATOR or ROLE_HR_SUPERVISOR. These roles are later on configured for web authorization, method authorization and domain object authorization. Other parts of Spring Security are capable of interpreting these authorities, and expect them to be present. GrantedAuthority objects are usually loaded by the UserDetailsService.

Usually the GrantedAuthority objects are application-wide permissions. They are not specific to a given domain object. Thus, you wouldn’t likely have a GrantedAuthority to represent a permission to Employee object number 54, because if there are thousands of such authorities you would quickly run out of memory (or, at the very least, cause the application to take a long time to authenticate a user). Of course, Spring Security is expressly designed to handle this common requirement, but you’d instead use the project’s domain object security capabilities for this purpose.

##### Summary
Just to recap, the major building blocks of Spring Security that we’ve seen so far are:
* **SecurityContextHolder**, to provide access to the SecurityContext.
* **SecurityContext**, to hold the Authentication and possibly request-specific security information.
* **Authentication**, to represent the **principal** in a Spring Security-specific manner.
* **GrantedAuthority**, to reflect the application-wide permissions granted to a **principal**.
* **UserDetails**, to provide the necessary information to build an Authentication object from your application’s DAOs or other source of security data.
* **UserDetailsService**, to create a UserDetails when passed in a String-based username (or certificate ID or the like).

### Authentication
Spring Security can participate in many different authentication environments. While we recommend people use Spring Security for authentication and not integrate with existing Container Managed Authentication, it is nevertheless supported - as is integrating with your own proprietary authentication system.

##### What is authentication in Spring Security?
Let’s consider a standard authentication scenario that everyone is familiar with:
* A user is prompted to log in with a username and password.
* The system (successfully) verifies that the password is correct for the username.
    * The username and password are obtained and combined into an instance of UsernamePasswordAuthenticationToken an instance of the Authentication interface.
    * The token is passed to an instance of AuthenticationManager for validation.
* The context information for that user is obtained (their list of roles and so on).
    * The AuthenticationManager returns a fully populated Authentication instance on successful authentication.
* A security context is established for the user
    * The security context is established by calling SecurityContextHolder.getContext().setAuthentication(…), passing in the returned authentication object.
* From that point on, the user is considered to be authenticated. 
* (optional) The user proceeds, potentially to perform some operation which is potentially protected by an access control mechanism which checks the required permissions for the operation against the current security context information.

###### ExceptionTranslationFilter
ExceptionTranslationFilter is a Spring Security filter that has responsibility for detecting any Spring Security exceptions that are thrown. Such exceptions will generally be thrown by an AbstractSecurityInterceptor, which is the main provider of authorization services. We will discuss AbstractSecurityInterceptor in the next section, but for now we just need to know that it produces Java exceptions and knows nothing about HTTP or how to go about authenticating a principal. Instead the ExceptionTranslationFilter offers this service, with specific responsibility for either returning error code 403 (if the principal has been authenticated and therefore simply lacks sufficient access - as per step seven above), or launching an AuthenticationEntryPoint (if the principal has not been authenticated and therefore we need to go commence step three).

###### AuthenticationEntryPoint
The AuthenticationEntryPoint is responsible for step three in the above list. As you can imagine, each web application will have a default authentication strategy (well, this can be configured like nearly everything else in Spring Security, but let’s keep it simple for now). Each major authentication system will have its own AuthenticationEntryPoint implementation, which typically performs one of the actions described in step 3.

###### Authentication Mechanism
Once your browser submits your authentication credentials (either as an HTTP form post or HTTP header) there needs to be something on the server that "collects" these authentication details. By now we’re at step six in the above list. In Spring Security we have a special name for the function of collecting authentication details from a user agent (usually a web browser), referring to it as the "authentication mechanism". Examples are form-base login and Basic authentication. Once the authentication details have been collected from the user agent, an Authentication "request" object is built and then presented to the AuthenticationManager.
After the authentication mechanism receives back the fully-populated Authentication object, it will deem the request valid, put the Authentication into the SecurityContextHolder, and cause the original request to be retried (step seven above). If, on the other hand, the AuthenticationManager rejected the request, the authentication mechanism will ask the user agent to retry (step two above).

###### Storing the SecurityContext between requests
Depending on the type of application, there may need to be a strategy in place to store the security context between user operations. In a typical web application, a user logs in once and is subsequently identified by their session Id. The server caches the principal information for the duration session. In Spring Security, the responsibility for storing the SecurityContext between requests falls to the SecurityContextPersistenceFilter, which by default stores the context as an HttpSession attribute between HTTP requests. It restores the context to the SecurityContextHolder for each request and, crucially, clears the SecurityContextHolder when the request completes. You shouldn’t interact directly with the HttpSession for security purposes. There is simply no justification for doing so - always use the SecurityContextHolder instead.
Many other types of application (for example, a stateless RESTful web service) do not use HTTP sessions and will re-authenticate on every request. However, it is still important that the SecurityContextPersistenceFilter is included in the chain to make sure that the SecurityContextHolder is cleared after each request.

##### Summary
Just to recap, the major building blocks of Authentication that we’ve seen so far are:
* A user is prompted to log in with a username and password.
* **AuthenticationManager**, verifies that the password is correct for the username.
* The security context is established by calling SecurityContextHolder.getContext().setAuthentication(…), passing in the returned authentication object.
* **SecurityContextPersistenceFilter**, stores the security context as an HttpSession attribute between HTTP requests.
* From that point on, the user is considered to be authenticated. 
* **ExceptionTranslationFilter**, translate security exception to HTTP codes.

### Access-Control (Authorization) in Spring Security
The main interface responsible for making access-control decisions in Spring Security is the AccessDecisionManager. It has a decide method which takes an Authentication object representing the principal requesting access, a "secure object" (see below) and a list of security metadata attributes which apply for the object (such as a list of roles which are required for access to be granted).

##### Security and AOP Advice
If you’re familiar with AOP, you’d be aware there are different types of advice available: before, after, throws and around. An around advice is very useful, because an advisor can elect whether or not to proceed with a method invocation, whether or not to modify the response, and whether or not to throw an exception. Spring Security provides an around advice for method invocations as well as web requests. We achieve an around advice for method invocations using Spring’s standard AOP support and we achieve an around advice for web requests using a standard Filter.

For those not familiar with AOP, the key point to understand is that Spring Security can help you protect method invocations as well as web requests. Most people are interested in securing method invocations on their services layer. This is because the services layer is where most business logic resides in current-generation Java EE applications. If you just need to secure method invocations in the services layer, Spring’s standard AOP will be adequate. If you need to secure domain objects directly, you will likely find that AspectJ is worth considering.

You can elect to perform method authorization using AspectJ or Spring AOP, or you can elect to perform web request authorization using filters. You can use zero, one, two or three of these approaches together. The mainstream usage pattern is to perform some web request authorization, coupled with some Spring AOP method invocation authorization on the services layer.

##### Secure Objects and the AbstractSecurityInterceptor
So what is a "secure object" anyway? Spring Security uses the term to refer to any object that can have security (such as an authorization decision) applied to it. The most common examples are method invocations and web requests.

Each supported secure object type has its own interceptor class, which is a subclass of AbstractSecurityInterceptor. Importantly, by the time the AbstractSecurityInterceptor is called, the SecurityContextHolder will contain a valid Authentication if the principal has been authenticated.

AbstractSecurityInterceptor provides a consistent workflow for handling secure object requests, typically:
* Look up the "configuration attributes" associated with the present request
* Submitting the secure object, current Authentication and configuration attributes to the AccessDecisionManager for an authorization decision
* Optionally change the Authentication under which the invocation takes place
* Allow the secure object invocation to proceed (assuming access was granted)
* Call the AfterInvocationManager if configured, once the invocation has returned. If the invocation raised an exception, the AfterInvocationManager will not be invoked.

###### What are Configuration Attributes?
A "configuration attribute" can be thought of as a String that has special meaning to the classes used by AbstractSecurityInterceptor. They are represented by the interface ConfigAttribute within the framework. They may be simple role names or have more complex meaning, depending on the how sophisticated the AccessDecisionManager implementation is. The AbstractSecurityInterceptor is configured with a SecurityMetadataSource which it uses to look up the attributes for a secure object. Usually this configuration will be hidden from the user. Configuration attributes will be entered as annotations on secured methods or as access attributes on secured URLs.

##### RunAsManager
Assuming AccessDecisionManager decides to allow the request, the AbstractSecurityInterceptor will normally just proceed with the request. Having said that, on rare occasions users may want to replace the Authentication inside the SecurityContext with a different Authentication, which is handled by the AccessDecisionManager calling a RunAsManager. This might be useful in reasonably unusual situations, such as if a services layer method needs to call a remote system and present a different identity. Because Spring Security automatically propagates security identity from one server to another (assuming you’re using a properly-configured RMI or HttpInvoker remoting protocol client), this may be useful.

##### Summary
Just to recap, the major building blocks of Authorization that we’ve seen so far are:
* **AccessDecisionManager**, the main interface responsible for making access-control decisions in Spring Security.
* **Secure Object**, the most common examples are method invocations and web requests.
    * **configuration attribute**, may be simple role names or have more complex meaning
* **AbstractSecurityInterceptor**, provides a consistent workflow for handling secure object requests.

### Core Services
Now that we have a high-level overview of the Spring Security architecture and its core classes, let’s take a closer look at one or two of the core interfaces and their implementations, in particular the AuthenticationManager, UserDetailsService and the AccessDecisionManager. These crop up regularly throughout the remainder of this document so it’s important you know how they are configured and how they operate.

##### The AuthenticationManager, ProviderManager and AuthenticationProvider
The AuthenticationManager is just an interface, so the implementation can be anything we choose, but how does it work in practice? What if we need to check multiple authentication databases or a combination of different authentication services such as a database and an LDAP server?

The default implementation in Spring Security is called ProviderManager and rather than handling the authentication request itself, it delegates to a list of configured AuthenticationProviders, each of which is queried in turn to see if it can perform the authentication. Each provider will either throw an exception or return a fully populated Authentication object. The most common approach to verifying an authentication request is to load the corresponding UserDetails and check the loaded password against the one that has been entered by the user. This is the approach used by the DaoAuthenticationProvider. The loaded UserDetails object - and particularly the GrantedAuthorities it contains - will be used when building the fully populated Authentication object which is returned from a successful authentication and stored in the SecurityContext.

If you are using the namespace, an instance of ProviderManager is created and maintained internally, and you add providers to it by using the namespace authentication provider elements. In this case, you should not declare a ProviderManager bean in your application context. However, if you are not using the namespace then you would declare it like so:

    <bean id="authenticationManager" class="org.springframework.security.authentication.ProviderManager">
    <constructor-arg>
        <list>
            <ref local="daoAuthenticationProvider"/>
            <ref local="anonymousAuthenticationProvider"/>
            <ref local="ldapAuthenticationProvider"/>
		</list>
	</constructor-arg>
    </bean>
In the above example we have three providers. They are tried in the order shown (which is implied by the use of a List), with each provider able to attempt authentication, or skip authentication by simply returning null. If all implementations return null, the ProviderManager will throw a ProviderNotFoundException. If you’re interested in learning more about chaining providers, please refer to the ProviderManager JavaDocs.

##### UserDetailsService
As mentioned in the earlier in this reference guide, most authentication providers take advantage of the UserDetails and UserDetailsService interfaces. Recall that the contract for UserDetailsService is a single method:

UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
The returned UserDetails is an interface that provides getters that guarantee non-null provision of authentication information such as the username, password, granted authorities and whether the user account is enabled or disabled. Most authentication providers will use a UserDetailsService, even if the username and password are not actually used as part of the authentication decision. They may use the returned UserDetails object just for its GrantedAuthority information, because some other system (like LDAP or X.509 or CAS etc) has undertaken the responsibility of actually validating the credentials.

Given UserDetailsService is so simple to implement, it should be easy for users to retrieve authentication information using a persistence strategy of their choice. Having said that, Spring Security does include a couple of useful base implementations, which we’ll look at below.

##### Password Encoding
Spring Security’s PasswordEncoder interface is used to support the use of passwords which are encoded in some way in persistent storage. You should never store passwords in plain text. Always use a one-way password hashing algorithm such as bcrypt which uses a built-in salt value which is different for each stored password. Do not use a plain hash function such as MD5 or SHA, or even a salted version. Bcrypt is deliberately designed to be slow and to hinder offline password cracking, whereas standard hash algorithms are fast and can easily be used to test thousands of passwords in parallel on custom hardware. You might think this doesn’t apply to you since your password database is secure and offline attacks aren’t a risk. If so, do some research and read up on all the high-profile sites which have been compromised in this way and have been pilloried for storing their passwords insecurely. It’s best to be on the safe side. Using org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder" is a good choice for security. There are also compatible implementations in other common programming languages so it a good choice for interoperability too.

If you are using a legacy system which already has hashed passwords, then you will need to use an encoder which matches your current algorithm, at least until you can migrate your users to a more secure scheme (usually this will involve asking the user to set a new password, since hashes are irreversible). Spring Security has a package containing legacy password encoding implementation, namely, org.springframework.security.authentication.encoding. The DaoAuthenticationProvider can be injected with either the new or legacy PasswordEncoder types.

### Authorization
The advanced authorization capabilities within Spring Security represent one of the most compelling reasons for its popularity. Irrespective of how you choose to authenticate - whether using a Spring Security-provided mechanism and provider, or integrating with a container or other non-Spring Security authentication authority - you will find the authorization services can be used within your application in a consistent and simple way.

In this part we’ll explore the different AbstractSecurityInterceptor implementations, which were introduced in Part Access-Control (Authorization) in Spring Security. We then move on to explore how to fine-tune authorization through use of domain access control lists.

##### Authorities
As we saw in the technical overview, all Authentication implementations store a list of GrantedAuthority objects. These represent the authorities that have been granted to the principal. The GrantedAuthority objects are inserted into the Authentication object by the AuthenticationManager and are later read by AccessDecisionManagers when making authorization decisions. GrantedAuthority is an interface with only one method: 

    String getAuthority().
    
This method allows AccessDecisionManagers to obtain a precise String representation of the GrantedAuthority. By returning a representation as a String, a GrantedAuthority can be easily "read" by most AccessDecisionManagers. If a GrantedAuthority cannot be precisely represented as a String, the GrantedAuthority is considered "complex" and getAuthority() must return null.

Spring Security includes one concrete GrantedAuthority implementation, GrantedAuthorityImpl. This allows any user-specified String to be converted into a GrantedAuthority. All AuthenticationProviders included with the security architecture use GrantedAuthorityImpl to populate the Authentication object.

##### The AccessDecisionManager
The AccessDecisionManager is called by the AbstractSecurityInterceptor and is responsible for making final access control decisions. the AccessDecisionManager interface contains three methods:

    void decide(Authentication authentication, Object secureObject;
    Collection<ConfigAttribute> attrs) throws AccessDeniedException;
    boolean supports(ConfigAttribute attribute);
    boolean supports(Class clazz);

The AccessDecisionManager's decide method is passed all the relevant information it needs in order to make an authorization decision. In particular, passing the secure Object enables those arguments contained in the actual secure object invocation to be inspected. For example, let’s assume the secure object was a MethodInvocation. It would be easy to query the MethodInvocation for any Customer argument, and then implement some sort of security logic in the AccessDecisionManager to ensure the principal is permitted to operate on that customer. Implementations are expected to throw an AccessDeniedException if access is denied.

The supports(ConfigAttribute) method is called by the AbstractSecurityInterceptor at startup time to determine if the AccessDecisionManager can process the passed ConfigAttribute. The supports(Class) method is called by a security interceptor implementation to ensure the configured AccessDecisionManager supports the type of secure object that the security interceptor will present.

##### Voting-Based AccessDecisionManager Implementations
Whilst users can implement their own AccessDecisionManager to control all aspects of authorization, Spring Security includes several AccessDecisionManager implementations that are based on voting. Figure 21.1, “Voting Decision Manager” illustrates the relevant classes.

##### Voting Decision Manager
Using this approach, a series of AccessDecisionVoter implementations are polled on an authorization decision. The AccessDecisionManager then decides whether or not to throw an AccessDeniedException based on its assessment of the votes.
The AccessDecisionVoter interface has three methods:

    int vote(Authentication authentication, Object object, Collection<ConfigAttribute> attrs);
    boolean supports(ConfigAttribute attribute);
    boolean supports(Class clazz);

Concrete implementations return an int, with possible values being reflected in the AccessDecisionVoter static fields ACCESS_ABSTAIN, ACCESS_DENIED and ACCESS_GRANTED. A voting implementation will return ACCESS_ABSTAIN if it has no opinion on an authorization decision. If it does have an opinion, it must return either ACCESS_DENIED or ACCESS_GRANTED.

There are three concrete AccessDecisionManagers provided with Spring Security that tally the votes. the ConsensusBased implementation will grant or deny access based on the consensus of non-abstain votes. Properties are provided to control behavior in the event of an equality of votes or if all votes are abstain. The AffirmativeBased implementation will grant access if one or more ACCESS_GRANTED votes were received (i.e. a deny vote will be ignored, provided there was at least one grant vote). Like the ConsensusBased implementation, there is a parameter that controls the behavior if all voters abstain. The UnanimousBased provider expects unanimous ACCESS_GRANTED votes in order to grant access, ignoring abstains. It will deny access if there is any ACCESS_DENIED vote. Like the other implementations, there is a parameter that controls the behaviour if all voters abstain.

##### RoleVoter
The most commonly used AccessDecisionVoter provided with Spring Security is the simple RoleVoter, which treats configuration attributes as simple role names and votes to grant access if the user has been assigned that role.

It will vote if any ConfigAttribute begins with the prefix ROLE_. It will vote to grant access if there is a GrantedAuthority which returns a String representation (via the getAuthority() method) exactly equal to one or more ConfigAttributes starting with the prefix ROLE_. If there is no exact match of any ConfigAttribute starting with ROLE_, the RoleVoter will vote to deny access. If no ConfigAttribute begins with ROLE_, the voter will abstain.

##### AuthenticatedVoter
Another voter which we’ve implicitly seen is the AuthenticatedVoter, which can be used to differentiate between anonymous, fully-authenticated and remember-me authenticated users. Many sites allow certain limited access under remember-me authentication, but require a user to confirm their identity by logging in for full access.
When we’ve used the attribute IS_AUTHENTICATED_ANONYMOUSLY to grant anonymous access, this attribute was being processed by the AuthenticatedVoter. See the Javadoc for this class for more information.

##### Custom Voters
Obviously, you can also implement a custom AccessDecisionVoter and you can put just about any access-control logic you want in it. It might be specific to your application (business-logic related) or it might implement some security administration logic. For example, you’ll find a blog article on the Spring web site which describes how to use a voter to deny access in real-time to users whose accounts have been suspended.

##### Hierarchical Roles
It is a common requirement that a particular role in an application should automatically "include" other roles. For example, in an application which has the concept of an "admin" and a "user" role, you may want an admin to be able to do everything a normal user can. To achieve this, you can either make sure that all admin users are also assigned the "user" role. Alternatively, you can modify every access constraint which requires the "user" role to also include the "admin" role. This can get quite complicated if you have a lot of different roles in your application.

The use of a role-hierarchy allows you to configure which roles (or authorities) should include others. An extended version of Spring Security’s RoleVoter, RoleHierarchyVoter, is configured with a RoleHierarchy, from which it obtains all the "reachable authorities" which the user is assigned. A typical configuration might look like this:

    <bean id="roleVoter" class="org.springframework.security.access.vote.RoleHierarchyVoter">
	    <constructor-arg ref="roleHierarchy" />
    </bean>
    <bean id="roleHierarchy"
		class="org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl">
	    <property name="hierarchy">
		    <value>
			    ROLE_ADMIN > ROLE_STAFF
			    ROLE_STAFF > ROLE_USER
			    ROLE_USER > ROLE_GUEST
		    </value>
	    </property>
    </bean>
Here we have four roles in a hierarchy ROLE_ADMIN ⇒ ROLE_STAFF ⇒ ROLE_USER ⇒ ROLE_GUEST. A user who is authenticated with ROLE_ADMIN, will behave as if they have all four roles when security contraints are evaluated against an AccessDecisionManager cconfigured with the above RoleHierarchyVoter. The > symbol can be thought of as meaning "includes".

Role hierarchies offer a convenient means of simplifying the access-control configuration data for your application and/or reducing the number of authorities which you need to assign to a user. For more complex requirements you may wish to define a logical mapping between the specific access-rights your application requires and the roles that are assigned to users, translating between the two when loading the user information.
