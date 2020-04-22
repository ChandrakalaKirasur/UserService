package com.bridgelabz.fundoonotesms.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.bridgelabz.fundoonotesms.entity.User;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

	public Optional<User> findByUserID(Long userID);

	public boolean deleteByUserID(Long userID);

	public Optional<User> findByEmailId(String emailID);

	public boolean existsByFirstName(String userName);

	public Optional<User> findByFirstName(String userName);

	/*
	 * default List<Note> getAllTrashedNotes(Long userID) { Optional<User> user =
	 * findByUserID(userID); return user.get().getNotes().stream().filter(p ->
	 * p.isTrashed() == true).collect(Collectors.toList()); }
	 * 
	 * default List<Note> getAllArchivedNotes(Long userID) { Optional<User> user =
	 * findByUserID(userID); return user.get().getNotes().stream().filter(p ->
	 * p.isArchieved() == true &&
	 * p.isTrashed()==false).collect(Collectors.toList()); }
	 * 
	 * default List<Note> findAllPinnedNotes(Long userID) { Optional<User> user =
	 * findByUserID(userID); return user.get().getNotes().stream().filter(p ->
	 * p.isPinned() == true).collect(Collectors.toList()); }
	 * 
	 * default List<Note> findAllReminderNotes(Long userID){ Optional<User> user =
	 * findByUserID(userID); return user.get().getNotes().stream().filter(p ->
	 * p.getReminderDate() != null).collect(Collectors.toList()); }
	 */

}
