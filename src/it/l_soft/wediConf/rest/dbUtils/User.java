package it.l_soft.wediConf.rest.dbUtils;

import java.util.UUID;

public class User extends DBInterface
{
	private static final long serialVersionUID = 5418709142850102984L;

	protected int idUser = 0;
	protected String email = "";
	protected String firstName = "";
	protected String lastName = "";
	protected String organization = "";
	protected String token = "";
	protected boolean active = false;
	
	private void setNames()
	{
		tableName = "users";
		idColName = "idUser";
	}

	public User()
	{
		setNames();
	}

	public User(DBConnection conn, int id) throws Exception
	{
		getUser(conn, id);
	}

	public void getUser(DBConnection conn, int id) throws Exception
	{
		setNames();
		String sql = "SELECT * " +
					 "FROM " + tableName + " " +
					 "WHERE " + idColName + " = " + id;
		this.populateObject(conn, sql, this);
	}

	public static User getUserByToken(DBConnection conn, String token) throws Exception
	{
		User user = null;
		String sql = "SELECT * " +
					 "FROM users " +
					 "WHERE token = '" + token + "'";
		try
		{
			user = (User) User.populateByQuery(conn, sql, User.class);
		}
		catch(Exception e)
		{
			if (e.getMessage().compareTo("No record found") == 0)
			{
				user = new User();
				user.token = UUID.randomUUID().toString();
				user.setIdUser(user.insertAndReturnId(conn, "idUser", user));
			}
			else
			{
				throw e;
			}
		}
		return user;
	}

	public int getIdUser() {
		return idUser;
	}

	public void setIdUser(int idUser) {
		this.idUser = idUser;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
	
}

