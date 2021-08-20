package com.java.everis.mstransactionfixedterm.service.impl;

import com.java.everis.mstransactionfixedterm.entity.FixedTerm;
import com.java.everis.mstransactionfixedterm.entity.TransactionFixedTerm;
import com.java.everis.mstransactionfixedterm.entity.TypeTransaction;
import com.java.everis.mstransactionfixedterm.repository.TransactionFixedTermRepository;
import com.java.everis.mstransactionfixedterm.service.TransactionFixedTermService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class TransactionFixedTermServiceImpl implements TransactionFixedTermService {

	private final WebClient webClient;
	private final ReactiveCircuitBreaker reactiveCircuitBreaker;
	
	String uri = "http://gateway:8090/api/ms-fixed-term/fixedTerm";
	
	public TransactionFixedTermServiceImpl(ReactiveResilience4JCircuitBreakerFactory circuitBreakerFactory) {
		this.webClient = WebClient.builder().baseUrl(this.uri).build();
		this.reactiveCircuitBreaker = circuitBreakerFactory.create("fixedTerm");
	}

    @Autowired
    private TransactionFixedTermRepository fixedTermRepository;

    // Plan A - FindId
    @Override
    public Mono<FixedTerm> findFixedTermById(String id) {
		return reactiveCircuitBreaker.run(webClient.get().uri(this.uri + "/find/{id}",id).accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(FixedTerm.class),
				throwable -> {
					return this.getDefaultFixedTerm();
				});
    }
    
   	// Plan A - Update
	@Override
    public Mono<FixedTerm> updateFixedTerm(FixedTerm ft) {
		return reactiveCircuitBreaker.run(webClient.put().uri(this.uri + "/update",ft).accept(MediaType.APPLICATION_JSON).syncBody(ft).retrieve().bodyToMono(FixedTerm.class),
				throwable -> {
					return this.getDefaultFixedTerm();
				});
    }
    
	
    // Plan B - FindId
   	public Mono<FixedTerm> getDefaultFixedTerm() {
   		Mono<FixedTerm> fixedTerm = Mono.just(new FixedTerm("0", null, null,null,null,null,null,null,null,null,null,null));
   		return fixedTerm;
   	}
   	
   	
    @Override
    public Mono<TransactionFixedTerm> create(TransactionFixedTerm t) {
        return fixedTermRepository.save(t);
    }

    @Override
    public Flux<TransactionFixedTerm> findAll() {
        return fixedTermRepository.findAll();
    }

    @Override
    public Mono<TransactionFixedTerm> findById(String id) {
        return fixedTermRepository.findById(id);
    }

    @Override
    public Mono<TransactionFixedTerm> update(TransactionFixedTerm t) {
        return fixedTermRepository.save(t);
    }

    @Override
    public Mono<Boolean> delete(String t) {
        return fixedTermRepository.findById(t)
                .flatMap(tar -> fixedTermRepository.delete(tar).then(Mono.just(Boolean.TRUE)))
                .defaultIfEmpty(Boolean.FALSE);
    }

    @Override
    public Mono<Long> countTransactions(String id, TypeTransaction typeTransaction) {
        return fixedTermRepository.findByFixedTermId(id)
                .filter(transactionFixedTerm -> transactionFixedTerm.getTypeTransaction().equals(typeTransaction))
                .count();
    }

}
