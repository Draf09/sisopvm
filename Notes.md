# Notes

## Concurrency

**Atomicity**, indivisible in time. in case our machine we can say that the instructions exeuted by out processor.

It is about the execution's platform

How can we implement our atomicity level?

Critical Section 

# Virtual memory
page fault , when the memory is not in the page table


Stallings - Operating Systems

Table 8.4 Operating System Policies for Virtual Memory

Page 393 Page fault 

OPT (optimal), LRU (least recent used), FIFO, CLOCK


- Fazer funcionar com a lista de bloqueados primeiro 


Interrupt system call, colocar o processo na lista de bloqueados 

id = ger proc.rruning.id
coloca bloqueados

gerProc.bloqueados.add(ger.proc.running) 

ver mais algum lugar sobe a lista de bloqueados
