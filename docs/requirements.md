# Operations Service Design Requirements
## Introduction  
The following requirements provide guidance and structure for implementing an Operations Service. 
Each requirement has been identified as an essential part of the architecture and must be incorporated to maximize value to administrators and customers.

## Scope  
These requirements are scoped to encompass both business and technical requirements for an Operations Service.  

## Requirements  
### Independent component  
![Generic badge](https://img.shields.io/badge/BUSINESS-MVP-GREEN.svg)  
As a user, I want the Operations Service to be an independent component,
so that I can version functionality and features, 
because functionality and features need to be incrementally added.

### Bootstrap customer clusters
![Generic badge](https://img.shields.io/badge/TECHNICAL-MVP-GREEN.svg)  
As a user, I want the Operations Service to bootstrap customer clusters, 
so that I do not need knowledge of the CrateKube services to create a Kubernetes cluster, 
because manually executing API calls is difficult and error prone.

#### Automated cluster bootstrap
![Generic badge](https://img.shields.io/badge/TECHNICAL-MVP-GREEN.svg)  
As a user I want the Operations Service to bootstrap clusters in an automated fashion, 
so that I can create clusters easily and without errors, 
because manual creation is difficult and error prone.

### Tie together management services
![Generic badge](https://img.shields.io/badge/BUSINESS-MVP-GREEN.svg)  
As a user I want the Operations Service to tie together management services, 
so that I can simplify operations, 
because manual management service interaction is difficult and error prone.

### Async for long running tasks  
![Generic badge](https://img.shields.io/badge/TECHNICAL-MVP-GREEN.svg)  
As a user, I want long running tasks to be handled asynchronously, 
so that cloud resources have enough time to be created, 
because creating cloud resources could take a long time and clients will timeout waiting for a synchronous response.  

#### Report state for long running tasks
![Generic badge](https://img.shields.io/badge/BUSINESS-MVP-GREEN.svg)  
As a user I want the Operations Service to report the state of long running tasks, 
so that I can know the exact state of long running tasks, 
because exact states are more meaningful than a complete/failed status.

### Security  
![Generic badge](https://img.shields.io/badge/BUSINESS-MVP-GREEN.svg)  
As a user, I want the Operations Service to be secure, 
so that my components are protected, 
because without security components may be manipulated by unauthorized users.   

#### token_authc and token_authz  
![Generic badge](https://img.shields.io/badge/TECHNICAL-MVP-GREEN.svg)  
As a user, I want token authentication and authorization implemented at runtime, 
so that REST resources are protected, 
because without security resources may be manipulated by unauthorized users.  

## Decisions made during requirements gathering  
The following decisions were made during requirements gathering:  

- `.`
