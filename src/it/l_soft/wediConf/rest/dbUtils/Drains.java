package it.l_soft.wediConf.rest.dbUtils;

import java.util.ArrayList;

import org.apache.log4j.Logger;

public class Drains extends DBInterface
{	
	private static final long serialVersionUID = -4849479160608801245L;
	
	protected int idDrains;
	protected String articleNumber;
	protected String description;
	protected String trayType;
	protected String drainDiameter; 
	protected double price;
	protected boolean selected = false;
	
	private void setNames()
	{
		tableName = "drains";
		idColName = "idDrains";
	}

	public Drains()
	{
		setNames();
	}

	public Drains(DBConnection conn, int id) throws Exception
	{
		getDrains(conn, id);
	}

	public void getDrains(DBConnection conn, int id) throws Exception
	{
		setNames();
		String sql = "SELECT * " +
					 "FROM " + tableName + " " +
					 "WHERE " + idColName + " = " + id;
		this.populateObject(conn, sql, this);
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<Drains> getDrains(DBConnection conn, String whereCondition, int languageCode) throws Exception {
		Logger log = Logger.getLogger(Drains.class);
		String sql = "SELECT * " +
					 "FROM drains " +
					 "WHERE " + whereCondition;
		log.trace("Querying: " + sql);
		return (ArrayList<Drains>) DBInterface.populateCollection(conn, sql, Drains.class);
	}

	public int getIdDrains() {
		return idDrains;
	}

	public void setIdDrains(int idDrains) {
		this.idDrains = idDrains;
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

	public String getTrayType() {
		return trayType;
	}

	public void setTrayType(String trayType) {
		this.trayType = trayType;
	}

	public String getDrainDiameter() {
		return drainDiameter;
	}

	public void setDrainDiameter(String drainDiameter) {
		this.drainDiameter = drainDiameter;
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
