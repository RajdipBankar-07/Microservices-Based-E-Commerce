package com.rajdip.ecommerce.service;

import com.rajdip.ecommerce.model.Counter;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

/**
 * Generates auto-incrementing Long IDs for MongoDB documents.
 * Uses a "db_sequences" collection with an atomic findAndModify.
 *
 * Usage: entity.setId(sequenceService.nextId("users"));
 */
@Service
public class SequenceGeneratorService {

    private final MongoOperations mongoOperations;

    public SequenceGeneratorService(MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }

    public long nextId(String sequenceName) {
        Counter counter = mongoOperations.findAndModify(
                Query.query(Criteria.where("_id").is(sequenceName)),
                new Update().inc("seq", 1),
                FindAndModifyOptions.options().returnNew(true).upsert(true),
                Counter.class
        );
        return counter == null ? 1L : counter.getSeq();
    }
}
