package org.mongodb.transaction.entity;

import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Reference;

@Entity(value = "user", noClassnameStored = true)
public class UserEntity
{
	@Id
	private ObjectId           id;
	private String             name;
	private int                age;

	@Reference("school")
	private SchoolEntity       school;
//	@Embedded("school")
//	private SchoolEntity       schoolDoc;
	@Reference
	private List<CourseEntity> courses;
//	@Embedded
//	private List<CourseEntity> coursesDoc;

	public ObjectId getId()
	{
		return id;
	}

	public void setId(ObjectId id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public int getAge()
	{
		return age;
	}

	public void setAge(int age)
	{
		this.age = age;
	}

	public SchoolEntity getSchool()
	{
		return school;
	}

	public void setSchool(SchoolEntity school)
	{
		this.school = school;
	}

//	public SchoolEntity getSchoolDoc()
//	{
//		return schoolDoc;
//	}
//
//	public void setSchoolDoc(SchoolEntity schoolDoc)
//	{
//		this.schoolDoc = schoolDoc;
//	}

	public List<CourseEntity> getCourses()
	{
		return courses;
	}

	public void setCourses(List<CourseEntity> courses)
	{
		this.courses = courses;
	}

//	public List<CourseEntity> getCoursesDoc()
//	{
//		return coursesDoc;
//	}
//
//	public void setCoursesDoc(List<CourseEntity> coursesDoc)
//	{
//		this.coursesDoc = coursesDoc;
//	}

}
