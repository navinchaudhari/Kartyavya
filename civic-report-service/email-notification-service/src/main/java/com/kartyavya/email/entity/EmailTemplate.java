package com.kartyavya.email.entity;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder                 // includes fields from both parent and child classes
@Document(collection = "email_templates")
/* 
 * @CompoundIndex  - speeds up queries that involve a specific combination of multiple fields.
 *  - Creates a single index using multiple fields together.
	- Sometimes queries filter by more than one field.
	- Instead of creating separate indexes, you create one combined index.
*/
@CompoundIndex(                          
        name = "template_version_idx",
        def = "{'template_key':1,'version':1}",
        unique = true
)                               
public class EmailTemplate extends BaseEntity {

    @Id
    private String id;

    @Field("template_key")
    @NotBlank
    private String templateKey;

    @Field("version")
    private Integer version;

    @Field("subject")
    private String subject;

    @Field("body")
    private String body;

    @Field("variables")
    private List<String> variables;

}