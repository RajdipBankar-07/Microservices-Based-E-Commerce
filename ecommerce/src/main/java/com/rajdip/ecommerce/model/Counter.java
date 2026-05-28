package com.rajdip.ecommerce.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/** Internal counter document used to simulate auto-increment Long IDs in MongoDB. */
@Document(collection = "db_sequences")
public class Counter {
    @Id
    private String id;   // e.g. "users", "products", "orders"
    private long   seq;

    public String getId()       { return id; }
    public long   getSeq()      { return seq; }
    public void   setId(String id)  { this.id = id; }
    public void   setSeq(long seq)  { this.seq = seq; }
}
