package com.bridgelabz.fundoonotesms.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long userID;
	private String firstName;
	private String lastName;
	private String password;
	private String phoneNum;
	private String emailId;
	private String profile;
	@Column(name = "verified", columnDefinition = "boolean default false")
	private boolean verified;

	/*
	 * @OneToMany(cascade = CascadeType.ALL)
	 * 
	 * @JoinColumn(name = "user_id") private List<Note> notes;
	 * 
	 * @OneToMany(cascade = CascadeType.ALL)
	 * 
	 * @JoinColumn(name = "user_id") private List<Label> labels;
	 * 
	 * @JsonIgnore
	 * 
	 * @ManyToMany(cascade = CascadeType.ALL)
	 * 
	 * @JoinTable(name="collaborator_notes")
	 * 
	 * @JoinColumn(referencedColumnName = "user_id") private List<Note>
	 * collaboratedNotes;
	 */

}
