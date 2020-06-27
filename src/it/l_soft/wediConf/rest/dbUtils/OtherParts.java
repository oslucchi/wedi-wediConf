package it.l_soft.wediConf.rest.dbUtils;

import java.util.ArrayList;

import org.apache.log4j.Logger;

public class OtherParts extends DBInterface
{	
	private static final long serialVersionUID = -4849479160608801245L;
	
	protected int idOtherParts;
	protected String articleNumber;
	protected String description;
	protected String packaging;
	protected double price;
	protected boolean selected = false;
	
	private void setNames()
	{
		tableName = "otherParts";
		idColName = "idOtherParts";
	}

	public OtherParts()
	{
		setNames();
	}

	public OtherParts(DBConnection conn, int id) throws Exception
	{
		getOtherParts(conn, id);
	}

	public void getOtherParts(DBConnection conn, int id) throws Exception
	{
		setNames();
		String sql = "SELECT * " +
					 "FROM " + tableName + " " +
					 "WHERE " + idColName + " = " + id;
		this.populateObject(conn, sql, this);
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<OtherParts> getOtherParts(DBConnection conn, String whereCondition, int languageCode) throws Exception {
		Logger log = Logger.getLogger(OtherParts.class);
		String sql = "SELECT * " +
					 "FROM otherParts " +
					 "WHERE " + whereCondition;
		log.trace("Querying: " + sql);
		return (ArrayList<OtherParts>) DBInterface.populateCollection(conn, sql, OtherParts.class);
	}

	public int getIdOtherParts() {
		return idOtherParts;
	}

	public void setIdOtherParts(int idOtherParts) {
		this.idOtherParts = idOtherParts;
	}

	public String getArticleNumber() {
		return articleNumber;
	}

	public void setArticleNumber(String articleNumber) {
		this.articleNumber = articleNumber;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPackaging() {
		return packaging;
	}

	public void setPackaging(String packaging) {
		this.packaging = packaging;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}	
}
