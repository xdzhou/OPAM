package com.sky.opam.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Student implements Parcelable
{
	public String login;
	public String name;
	public String email;
	public SchoolEnum school;
	public String grade;
	
	public Student(SchoolEnum school) 
	{
		this.school = school;
	}
	
	public Student(Parcel in) 
	{
		this.login = in.readString();
		this.name = in.readString();
		this.email = in.readString();
		this.school = SchoolEnum.getEnumByIndex(in.readInt());
		this.grade = in.readString();
	}
	
	public enum SchoolEnum
	{
		TSP("Télécom SudParis", "TSP"),
		TEM("Télécom Ecole de Management", "TEM");
		
		private String schoolName;
		private String abbreviation;
		
		private SchoolEnum(String schoolName, String abbreviation)
		{
			this.schoolName = schoolName;
			this.abbreviation = abbreviation;
		}

		public String getSchoolName() 
		{
			return schoolName;
		}

		public String getAbbreviation() 
		{
			return abbreviation;
		}
		
		public int getIndex()
		{
			int index = -1;
			for(int i = 0; i < values().length; i++)
			{
				if(this.equals(values()[i]))
				{
					index = i;
					break;
				}
			}
			return index;
		}
		
		public static SchoolEnum getEnumByIndex(int index)
		{
			if(index >=0 && index < values().length)
				return values()[index];
			else
				return null;
		}
	}

	@Override
	public int describeContents() 
	{
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) 
	{
		dest.writeString(login);
		dest.writeString(name);
		dest.writeString(email);
		dest.writeInt(school.getIndex());
		dest.writeString(grade);
	}
	
	static final Parcelable.Creator<Student> CREATOR = new Parcelable.Creator<Student>() 
	{
		public Student createFromParcel(Parcel in) 
		{
		    return new Student(in);
		}
		
		public Student[] newArray(int size) 
		{
		    return new Student[size];
		}
	};
}
