# Antaeus billing process

Welcome to my solution :)


# Overview 

As considering the main problem: *to build the logic that will schedule payment of those invoices on the first of the
 month*, a simple solution comes at the very beginning but multiple constraints were found on the road which became
  such simple initial idea into a more robust (sophisticated) solution which can face the implications expected from
   well designed software. Thus, following concerns where considered when solving this challenge: 
   
   *code maintainability, scalability, efficiency, resilience, concurrency, responsiveness, effective thread (CPU) usage*

In order to cover this, following concepts were taken into consideration: 
 - DDD, Hexagonal Architecture, Even-Driven, functional programming, Reactive, Async, NonBlocking.

On the other hand, in order to achieve a light solution, I preferred to avoid any heavy frameworks (aka Spring) in
favor of a most simple but also powerful library as [Vertx](https://vertx.io/)
 
# Solution 
## Desired solution

### Billing general process
As starting point, we want our solution does something like this

![img](billingProcess.jpg) 

### Payment process
And as for payment process, something like this:

![img](paymentProcess.jpg) 
 
# Domain Designing
As we could see, current solution works around two main actors in order to handle the entire billing process: 
*Billing* and *Payment*. I did not forge about *Invoice*, this would be directly affected when processing its related
 *Payment*.

![img](domainContext.jpg) 

## Billing 
This entity represents the entire billing payment (scheduled) process to be triggered on the first of the month, this will send to execute a payment for every pending invoice grouped under this entity.

|Entity  |Description  |
|--|--|
|`Billing` | Created every time a new billing process is being triggered (1st of the month)  |
|`BillingInvoice` | Summarized invoices reference representing the ones which will be attempted to paid along this billing process  |

Actions taken over this context would be expressed as _Commands_.

```
StartBillingCommand 
CloseBillingInvoiceCommand 
```

Following the same idea, commands will return Events by confirming that requested action was successfully processed

```
BillingStartedEvent
BillingInvoiceCompletedEvent
BillingCompletedEvent
```


## Payment 
 Represents the *invoice* payment transaction as a whole, considering all the steps (status) taken in order to
  consider a pending invoice successfully processed (paid and charged). This transaction will have a life cycle
   defined by following status: 
 
| Payment Status | Description  |
|--|--|
| `STARTED` | New payment requested |
| `COMPLETED`| Payment completed, both: PAID and CHARGE successfully processed |
| `CANCELED` | Something failed, referred invoice should remain in its initial status (PENDING) |

Again,actions over this context domain will be expressed as _Commands_

|Command    | Description |
|--|--|
|`CreatePaymentCommand`   | Create new command |
|`PayInvoiceCommand`  | Pay invoice, set invoice as PAID |
|`ChargeInvoiceCommand`| Charge invoice through payment provider |
|`CompletePaymentCommand`| Complete payment transaction |
|`CancelPaymentCommand`| Cancel payment transaction |
|`RevertPaymentCommand`| Revert payment to PENDING |   

Considering this commands, we will have these _Events_: 

```
PaymentCreatedEvent
InvoicePaidEvent
InvoiceChargedEvent
PaymentCompletedEvent
PaymentRevertedEvent
PaymentCanceledEvent
```
 

# Architecture

As regarding what it was described, implementation is thought to be scalable, resilient and  responsive. Then, a
 message driven (reactive) approach is the most who adapts for this purpose. Then, following diagram explains how
  all above described interacts 
  
![img](architecture.jpg)   

From above workflow we have that:
 - _Commands_ are requested by being sent through a message bus
 - A _Command Handler_ consumes such command in order to perform related action over the domain (core)
 - Once this is completed, an _Event_ is sent as notification of a successful command execution
 - [_Saga_](https://microservices.io/patterns/data/saga.html) pattern is being used as a way to orchestrate subsequent actions caused as a result of any past action
  performed (Event) in order to manage the payment transaction as a whole

# Scheduler
Scheduler is implemented by using [Quartz](http://www.quartz-scheduler.org/) as considering it manages cron
 expressions. Thus, following cron expresion has been setup insider app runner `Antaeus.app`: 
 ```
var quarzVerticle = QuarzVerticle(scheduler, JobKey("Billing Job"), "0 0 0 1 1/1 ? *", billingService)
```

Where `0 0 0 1 1/1 ? *` represents first day of the month.

 # Endpoints
 
As for verification, following endpoints were created in order to check payments and billing final status. 
### Payments
Endpoint
``` GET -  http://localhost:7000/rest/v1/payments ```

Response
```
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
```
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
...
   },
   "complete":true
}
```

## Code structure

It's following hexagonal arquitecture where:
 - `pleo-antaeus-core`: contains all domain core business code agnostic of any framework avoiding such dependencies
 - `pleo-antaeus-infra`: Represent infrastucture implementation, such a database implementation
 - `pleo-antaeus-app`: Orchestrate domain business logic by considering main Use Case: Billing Processing
 - `pleo-antaeus-rest`: Containing all endpoints
 

# Technical Caveats
1. As explained, Saga pattern will orchestrate commands -> event -> next command sequences. This as asynchronous action
. That is why async command/event bus are so important as event driver engine.
2. Vertx is used in order to define actor model pattern
3. As general rule, I've tried to avoid any blocking call. This way the even loop is more responsive and able to
 manage more request as comparing with blocking solution. Database access were implemented considering this.
