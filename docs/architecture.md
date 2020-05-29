# Operations Service Architecture Design
## Overview    
The `operations-service` is responsible for tying together the different management services and allows us to automate the full end-to-end experience for our users. 
It will interact directly with the cloud-mgmt-service and the cluster-mgmt-service to create cloud resources and Kubernetes clusters.  

The MVP targets AWS and provides automated management for CrateKube. 
After initial provisioning, this should be the primary service that users interact with to make changes.

Authentication will be set at runtime using a shared token, to be superseded by more robust authentication in the future.  

## Components 
`The following architecture is comprised of multiple components.`  

#### operations-service
The primary service in this architecture. See [Overview](#overview) for roles and responsibilities. 

#### cloud-mgmt-service  
A microservice developed by the CrateKube team responsible for provisioning and monitoring cloud resources and services. 

#### cluster-mgmt-service  
A microservice developed by the CrateKube team responsible for providing everything needed for creating, monitoring and deleting a Kubernetes cluster and configuring it post-bootstrap.   

## Diagrams
#### Component and Logical

![Component and Logical](http://www.plantuml.com/plantuml/png/ZP2_IiSm58NtFiMbWbkWBY8EKapff0YAapXSqsi8vaz9Rbp4TxUb4iJG7prJ0j_vSaYV62DdPnisR0aUPeh89lW4JnG_ZI8G88ERWoVFS0vtGHk55WQ3Eg9hOCkCwWCrGIjjo1FSerzkRgtyQbhuMjcI5xM2mcd78ct8AvnrtRYaWD_bLFpLmDeS2lB7F8wtb_BYb5dQd9ZYeUqR-d_zEqrNRzKRUvE4kDbLNODyV1wEzCitrksU_9IT_G40)  

<details><summary>Show UML Code</summary>
<p>
  
```
@startuml
title Operations Service - Component & Logical Diagram
       package "Cloud Management Service" {
            [cloud-mgmt-service] #00FFFF
        }
        package "Operations Service" {
            [operations-service] #FFB6C1
        }
        package "Cluster Management Service" {
            [cluster-mgmt-service] #fed8b1
        }
        [operations-service] -->  [cluster-mgmt-service] : CRUD
        [operations-service] -->  [cloud-mgmt-service] : CRUD
@enduml
```
  
</p>
</details>

#### Physical

![Physical](http://www.plantuml.com/plantuml/png/jP5DZzCm48Rl-HL3k82qRXOE2Eq1RPjjJ-L3AXA7rHuczcnYhJyYUxnLXVhVSH9QHHGG5-QGAFQZvtr7kOsCWPCrnGljf4cAe6FkuqrL1TmlMirTQbAa8BrZShoGtbQuZmQREBo5pXTHFFeWUeDkC5KM8rEeb8vSBR_jQc-jTul-_uLy8jxxyAHTktIgdXntPFGpXKErtgwMVM-qHDRURJsg5MQ9f31LOis6u7DFOGGYx2WyQqaWu4jtY5gYWJkDRK1RIEEJWdNz5huNaEVFxbTecgjoE87JKNbLVjtkOEsmCQJArwCXOo22RZiM7-1-rg8sQLZm0MuW-CGKHznvHL1UOkLOQ9eUvHEs6Vjv1n_HvMzBZa-xpV5XxkM42jjQ5d627k3b8wdtpPipUlnTXa_gtn8o-Al_-dgJPoB-bdBnToxz_epC_9lDwbqzxpyzZyKjEPMi-G40)  

<details><summary>Show UML Code</summary>
<p>
  
```
@startuml
!include https://raw.githubusercontent.com/awslabs/aws-icons-for-plantuml/master/dist/AWSCommon.puml
!include https://raw.githubusercontent.com/awslabs/aws-icons-for-plantuml/master/dist/NetworkingAndContentDelivery/ELBApplicationLoadBalancer.puml
title Operations Service - Physical Diagram
cloud EC2 {
    ELBApplicationLoadBalancer(alb,"Load Balancer","TLS Enabled")
    alb -right-> [Operations Cluster] : routes
    node "Operations Cluster" {
        package "Cluster Management Service" {
            [cluster-mgmt-service] #fed8b1
        }
        package "Cloud Management Service" {
            [cloud-mgmt-service] #00FFFF      
        }
        package "Operations Service" {
            [operations-service] #FFB6C1
        }
    }
}
@enduml
```
  
</p>
</details>

#### Security 

![Security](http://www.plantuml.com/plantuml/png/dP1FImCn4CNl-HH3lHge5n4FKgpiJJnuL94n6RUX-RCa4q5Blhl1g0InjEmx9PFVlCoyLMACd9qLRDWIt4qKaKtm2UuflXb58Ej9vMZu7MuCZX6Ty44JB3fsRNDYYWlO2QYQK6rnhCXt7MxHrxiZpp-s3MrwK0sJRdGiKmCU_Ox3blmpPjvyb2SuUILzyNBsrLNwR_f9_hKFly_xpOVX-dpTcn_J9UNLaSDVGbTGfx1XdbBv6rR8-d1K5JeGr78vLCtRfRhFsgI85NcTdVq4)  

<details><summary>Show UML Code</summary>
<p>
  
```
@startuml
title Operations Service - Security Diagram
node "Operations Cluster" {
    package "Cluster Management Service" {
        [cluster-mgmt-service\n{token_authz}] #fed8b1
    }
    package "Operations Service" {
        [operations-service\n{token_authz}] #FFB6C1
    }
    [operations-service\n{token_authz}] --> [cluster-mgmt-service\n{token_authz}] : {token_authc, https}
    package "Cloud Management Service" {
        [cloud-mgmt-service\n{token_authz}] #00FFFF
    }
    [operations-service\n{token_authz}] --> [cloud-mgmt-service\n{token_authz}] : {token_authc, https}
}
@enduml
```
  
</p>
</details>
