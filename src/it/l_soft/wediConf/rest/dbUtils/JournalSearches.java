package it.l_soft.wediConf.rest.dbUtils;

import java.util.Date;

public class JournalSearches extends DBInterface
{
	private static final long serialVersionUID = -7190105174808002438L;

	protected int idJournalSearches = 0;
	protected String session = "";
	protected Date timestamp = null;
	protected String searchCriteria = "";

	private void setNames()
	{
		tableName = "journalSearches";
		idColName = "idJournalSearches";
	}

	public JournalSearches()
	{
		setNames();
	}

	public JournalSearches(DBConnection conn, int id) throws Exception
	{
		getJournalSearches(conn, id);
	}

	public void getJournalSearches(DBConnection conn, int id) throws Exception
	{
		setNames();
		String sql = "SELECT * " +
					 "FROM " + tableName + " " +
					 "WHERE " + idColName + " = " + id;
		this.populateObject(conn, sql, this);
	}

	public int getIdJournalSearches() {
		return idJournalSearches;
	}

	public void setIdJournalSearches(int idJournalSearches) {
		this.idJournalSearches = idJournalSearches;
	}

	public String getSession() {
		return session;
	}

	public void setSession(String session) {
		this.session = session;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getSearchCriteria() {
		return searchCriteria;
	}

	public void setSearchCriteria(String searchCriteria) {
		this.searchCriteria = searchCriteria;
	}
}

