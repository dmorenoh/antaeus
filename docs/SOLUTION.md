
# Antaeus billing process  
  
Welcome to my solution :)  
  
  
# Overview   
In response to the challenge: *to build the logic that will schedule payment of the invoices on the first of the  
 month*, I initially considered a simple approach, but as multiple obstacles appeared during the process, this simple idea developed into a more robust and sophisticated solution. Thus, the following concerns were considered when solving this challenge:   
     
   *code maintainability, scalability, efficiency, resilience, concurrency, responsiveness, effective thread (CPU) usage*  
  
To do this I referenced the following concepts:   
 - DDD, Hexagonal Architecture, Even-Driven, functional programming, Reactive, Async, NonBlocking.  
  
On the other hand, in order to achieve a lightweight solution, I avoided any heavy frameworks (aka Spring) and instead used a simpler but also powerful library, [Vertx](https://vertx.io/)  
   
# Solution 
## Desired solution  
  
### Billing general process  
As a starting point, the solution should do the following:

![img](billingProcess.jpg)   
  
### Payment process  
And the payment process should follow this workflow:  
  
![img](paymentProcess.jpg)   
   
# Domain Designing  
This demonstrates that the current solution works around two main actors in order to handle the entire billing process:   *Billing* and *Payment*. It is important to note here that *Invoice* was not ignored, but would be directly affected when processing its related  *Payment*.  
  
![img](domainContext.jpg)   
  
## Billing
This entity represents the entire billing payment (scheduled) process to be triggered on the first of the month. This will make the request to execute the payment for every pending invoice grouped under this entity.  
  
|Entity  |Description  |  
|--|--|  
|`Billing` | Created every time a new billing process is being triggered (1st of the month)  |  
|`BillingInvoice` | A summarised invoice's reference representing the ones which will be attempted to be paid along this billing process  |  
  
Actions taken over this context will be expressed as _Commands_.  
  
```  
StartBillingCommand 
CloseBillingInvoiceCommand 
```  
  
Following the same idea, commands will return Events by confirming that the requested action was successfully processed.  
  
```  
BillingStartedEvent  
BillingInvoiceCompletedEvent  
BillingCompletedEvent  
```  
  
  
## Payment   
 This represents the *invoice* payment transaction as a whole, considering all the steps (status) taken in order to consider a pending invoice successfully processed (paid and charged). This transaction will have a life cycle defined by the following statuses   
   
| Payment Status | Description  |  
|--|--|  
| `STARTED` | New payment requested |  
| `COMPLETED`| Payment completed: both PAID and CHARGED successfully processed |  
| `CANCELED` | Something failed. Referred invoice should remain in its initial status (PENDING) |  
  
As mentioned previously, actions over this context domain will be expressed as _Commands_.  
  
|Command    | Description |  
|--|--|  
|`CreatePaymentCommand` | Create new command |  
|`PayInvoiceCommand` | Pay invoice, set invoice as PAID |  
|`ChargeInvoiceCommand`| Charge invoice through payment provider |  
|`CompletePaymentCommand`| Complete payment transaction |  
|`CancelPaymentCommand`| Cancel payment transaction |  
|`RevertPaymentCommand`| Revert payment to PENDING |     
  
Because of these commands, we will have the following _Events_:   
  
```  
PaymentCreatedEvent  
InvoicePaidEvent  
InvoiceChargedEvent  
PaymentCompletedEvent  
PaymentRevertedEvent  
PaymentCanceledEvent  
```  
    
# Architecture  
  
Regarding what was previously described, implementation aims to be scalable, resilient and  responsive. Therefore, a  message driven (reactive) approach is one of the best options to achieve this. The following diagram explains how the actions described above interact with one another:   
    
![img](architecture.jpg)     
  
From this workflow we see that:  
 - _Commands_ are requested by being sent through a message bus  
 - A _Command Handler_ consumes such a command in order to perform a related action over the business domain (core)  
 - Once this is completed, an _Event_ is sent as a notification of a successful command execution  
 - [_Saga_](https://microservices.io/patterns/data/saga.html) pattern is being used as a way to orchestrate subsequent actions caused as a result of any past action performed (Event) in order to manage the payment transaction as a whole  
  
## Command Handler  
  
```kotlin  
 suspend fun handle(command: Command): Either<Throwable, Event> = when (command) {
         is CreatePaymentCommand -> paymentService.execute(command)
         is PayInvoiceCommand -> invoiceService.execute(command)
         is ChargeInvoiceCommand -> invoiceService.execute(command)
         is CompletePaymentCommand -> paymentService.execute(command)
         is RevertPaymentCommand -> invoiceService.execute(command)
         is CancelPaymentCommand -> paymentService.execute(command)
         else -> Either.left(RuntimeException("Invalid command ${command::class.simpleName}"))
     }
 ```  
  
## Event handler  
```kotlin  
 suspend fun handle(event: Event) {
        when (event) {
            is PaymentCreatedEvent -> paymentSaga.on(event)
            is InvoicePaidEvent -> paymentSaga.on(event)
            is InvoiceChargedEvent -> paymentSaga.on(event)
            is PaymentRevertedEvent -> paymentSaga.on(event)
        }
    }
```  
  
# Scheduler  
The scheduler is implemented by using [Quartz](http://www.quartz-scheduler.org/) because it manages _cron  expression_. Thus, the following _cron expression_ has been set up inside the app runner `Antaeus.app`:   
 ```kotlin
 var quarzVerticle = QuarzVerticle(scheduler, JobKey("Billing Job"), "0 0 0 1 1/1 ? *", billingService)  
```  
  
Where `0 0 0 1 1/1 ? *` represents first day of the month.  
  
 # Endpoints  
  As for verification, the following endpoints were created in order to check the final status of  _Payments_ and _Billing_.   
  
### Payments  
Endpoint  
``` GET -  http://localhost:7000/rest/v1/payments ```  
  
Response  
```  json
{
   "transactionId":"93db46a2-7a7e-4480-b231-9cd5e7845d9f",
   "invoiceId":1,
   "status":"COMPLETED",
   "cancellationReason":"N/A",
   "billingId":"11624cf9-b491-4b5e-b84a-23e206924913"
}
```  
  
### Billing  
Endpoint  
``` GET -  http://localhost:7000/rest/v1/billing ```  
  
Response  
```  json
{
   "processId":"11624cf9-b491-4b5e-b84a-23e206924913",
   "status":"COMPLETED",
   "invoices":{
      "1":{
         "billingId":"11624cf9-b491-4b5e-b84a-23e206924913",
         "invoiceId":1,
         "invoiceStatus":"PROCESSED"
      },
      "11":{
         "billingId":"11624cf9-b491-4b5e-b84a-23e206924913",
         "invoiceId":11,
         "invoiceStatus":"PROCESSED"
      },
      "21":{
         "billingId":"11624cf9-b491-4b5e-b84a-23e206924913",
         "invoiceId":21,
         "invoiceStatus":"PROCESSED"
      },
      "..."
   },
   "complete":true
} 
```  
  
## Code structure  
  
The code structure is following the hexagonal architecture where:  
 - `pleo-antaeus-core`: contains all domain core business code agnostic of any framework avoiding such dependencies  
 - `pleo-antaeus-infra`: Represents infrastructure implementation, such a database access 
 - `pleo-antaeus-app`: Orchestrates domain business logic by considering main Use Case: Billing Processing and Payment Transaction  
 - `pleo-antaeus-rest`: Containing all endpoints   
   
  
# Technical Caveats  
1. As explained, the Saga pattern will orchestrate commands -> event -> next command sequences, as asynchronous actions. That is why the async command/event bus are so important as the event-driven engine.  
2. Vertx is used in order to define the actor model pattern for both Billing and Payments.  
3. As a general rule, I've tried to avoid any blocking call. This way the [even loop](https://vertx.io/docs/vertx-core/java/#golden_rule) is more responsive and able to
 manage more requests in comparison with any blocking solution. As a result, database accesses were implemented by considering this.

# Run the app
```
./gradlew build
./gradlew run  
```