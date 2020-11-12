package it.l_soft.wediConf.rest.dbUtils;

public class JournalProposals extends DBInterface
{
	private static final long serialVersionUID = -7498787217634553652L;

	protected int idJournalProposals = 0;
	protected String session = "";
	protected String reference = "";
	protected String orderObject = "";

	private void setNames()
	{
		tableName = "journalProposals";
		idColName = "idJournalProposals";
	}

	public JournalProposals()
	{
		setNames();
	}

	public JournalProposals(DBConnection conn, int id) throws Exception
	{
		getJournalProposals(conn, id);
	}

	public void getJournalProposals(DBConnection conn, int id) throws Exception
	{
		setNames();
		String sql = "SELECT * " +
					 "FROM " + tableName + " " +
					 "WHERE " + idColName + " = " + id;
		this.populateObject(conn, sql, this);
	}

	public int getIdJournalProposals() {
		return idJournalProposals;
	}

	public void setIdJournalProposals(int idJournalProposals) {
		this.idJournalProposals = idJournalProposals;
	}

	public String getSession() {
		return session;
	}

	public void setSession(String session) {
		this.session = session;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public String getOrderObject() {
		return orderObject;
	}

	public void setOrderObject(String orderObject) {
		this.orderObject = orderObject;
	}
}

