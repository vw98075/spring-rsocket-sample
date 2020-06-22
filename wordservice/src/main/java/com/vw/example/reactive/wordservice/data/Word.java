package com.vw.example.reactive.wordservice.data;

//import org.hibernate.annotations.Cache;
//import org.hibernate.annotations.CacheConcurrencyStrategy;

import org.springframework.data.annotation.Id;

//import javax.persistence.*;
//import javax.validation.constraints.*;
//import javax.persistence.Entity;
import java.io.Serializable;

/**
 * A Word.
 */
//@Entity
//@Table(name = "word")
//@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Word implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
//    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
//    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

//    @NotNull
//    @Size(min = 1, max = 255)
//    @Column(name = "text", length = 255, nullable = false, unique = true)
    private String text;

    public Word() {
    }

    public Word(/*@NotNull @Size(min = 1, max = 255) */String text) {
        this.text = text;
    }

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public Word text(String text) {
        this.text = text;
        return this;
    }

    public void setText(String text) {
        this.text = text;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Word)) {
            return false;
        }
        return id != null && id.equals(((Word) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "Word{" +
            "id=" + getId() +
            ", text='" + getText() + "'" +
            "}";
    }
}
