package it.l_soft.wediConf.rest.dbUtils;

import java.util.Date;
import java.util.UUID;

public class JournalSessions extends DBInterface
{
	private static final long serialVersionUID = -4301838716902796017L;

	protected int idJournalSessions = 0;
	protected int idUser = 0;
	protected Date timestamp = null;
	protected String session= "";

	private void setNames()
	{
		tableName = "journalSessions";
		idColName = "idJournalSessions";
	}

	public JournalSessions()
	{
		setNames();
	}

	public JournalSessions(DBConnection conn, int id) throws Exception
	{
		getJournalSessions(conn, id);
	}

	public void getJournalSessions(DBConnection conn, int id) throws Exception
	{
		setNames();
		String sql = "SELECT * " +
					 "FROM " + tableName + " " +
					 "WHERE " + idColName + " = " + id;
		this.populateObject(conn, sql, this);
	}

	public static JournalSessions getBySessionId(DBConnection conn, String sessionId) throws Exception
	{
		if (sessionId.compareTo("") == 0)
		{
			JournalSessions js = new JournalSessions();
			js.session = UUID.randomUUID().toString();
			return js;
		}
		else
		{	
			String sql = "SELECT * " +
						 "FROM journalSessions " +
						 "WHERE session = '" + sessionId + "'";
			return (JournalSessions) JournalSessions.populateByQuery(conn, sql, JournalSessions.class);
		}
	}

	
	public int getIdJournalSessions() {
		return idJournalSessions;
	}

	public void setIdJournalSessions(int idJournalSessions) {
		this.idJournalSessions = idJournalSessions;
	}

	public int getIdUser() {
		return idUser;
	}

	public void setIdUser(int idUser) {
		this.idUser = idUser;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getSession() {
		return session;
	}

	public void setSession(String session) {
		this.session = session;
	}
}

