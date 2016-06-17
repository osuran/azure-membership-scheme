# Azure Membership Scheme

Azure membership scheme provides features for automatically discovering WSO2 Carbon server clusters on Azure.

##How It Works

Once a Carbon server starts it will query Virtual Machine IP addresses in the given cluster via Azure API. Before the cluster starts, it should be ensured that all of the virtual machines in the cluster are added to a particular network security group. Thereafter Hazelcast network configuration will be updated with the above IP addresses. As a result the Hazelcast instance will get connected all the other members in the cluster. In addition once a new member is added to the cluster, all the other members will get connected to the new member.

##Installation

1. For Azure Membership Scheme to work, Hazelcast configuration should be made pluggable. This has to be enabled in the products in different ways. For WSO2 products that are based on Carbon 4.2.0, [apply kernel patch0012](https://docs.wso2.com/display/Carbon420/Applying+a+Patch+to+the+Kernel). For Carbon 4.4.1 based products apply [patch0005](http://product-dist.wso2.com/downloads/carbon/4.4.1/patch0005/WSO2-CARBON-PATCH-4.4.1-0005.zip). These patches include a modification in the Carbon Core component for allowing to add third party membership schemes. WSO2 products that are based on Carbon versions later than 4.4.1 do not need any patches to be applied (To determine the Carbon version of a particular product, please refer to the [WSO2 Release Matrix](http://wso2.com/products/carbon/release-matrix/)).

2. Copy following JAR files to the repository/components/lib directory of the Carbon server:

 These JAR files are packaged with the ZIP distribution of the Kubernetes Membership Scheme, inside the lib folder.
 
 _accessors-smart-1.1.jar  
 bcprov-jdk15on-1.51.jar  
 gson-2.2.4.jar        
 jcip-annotations-1.0.jar  
 lang-tag-1.4.jar                     
 nimbus-jose-jwt-3.1.2.jar
 activation-1.1.jar       
 commons-codec-1.9.jar    
 httpclient-4.5.jar    
 json-2.0.0.wso2v1.jar     
 mail-1.4.7.jar                       
 oauth2-oidc-sdk-4.5.jar
 adal4j-0.0.2.jar         
 commons-lang3-3.3.1.jar  
 httpcore-4.4.1.jar    
 json-smart-2.2.1.jar      
 membershipScheme-1.0-SNAPSHOT.jar    
 slf4j-api-1.7.5.jar
 asm-5.0.3.jar            
 commons-logging-1.2.jar  
 java-json-0.13.0.jar  
 junit-3.8.1.jar_
 
3. 
