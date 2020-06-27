package it.l_soft.wediConf.rest.dbUtils;

import java.util.ArrayList;

import org.apache.log4j.Logger;

public class Profiles extends DBInterface {
	private static final long serialVersionUID = -4849479160608801245L;
	
	protected int idProfiles;
    protected String articleNumber;
    protected String description;
    protected String profileType;
    protected String side;
	protected int length;
	protected double tileHeight;
	protected double price;
    protected boolean selected;

	private void setNames()
	{
		tableName = "profiles";
		idColName = "idProfiles";
	}

	public Profiles()
	{
		setNames();
	}

	public Profiles(DBConnection conn, int id) throws Exception
	{
		getProfiles(conn, id);
	}

	public void getProfiles(DBConnection conn, int id) throws Exception
	{
		setNames();
		String sql = "SELECT * " +
					 "FROM " + tableName + " " +
					 "WHERE " + idColName + " = " + id;
		this.populateObject(conn, sql, this);
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<Profiles> getProfiles(DBConnection conn, String whereCondition, int languageCode) throws Exception {
		Logger log = Logger.getLogger(Profiles.class);
		String sql = "SELECT * " +
				 "FROM profiles " +
				 "WHERE " + whereCondition;
		log.trace("Querying: " + sql);
		return (ArrayList<Profiles>) DBInterface.populateCollection(conn, sql, Profiles.class);
	}

	public int getIdProfiles() {
		return idProfiles;
	}

	public void setIdProfiles(int idProfiles) {
		this.idProfiles = idProfiles;
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

	public String getProfileType() {
		return profileType;
	}

	public void setProfileType(String profileType) {
		this.profileType = profileType;
	}

	public String getSide() {
		return side;
	}

	public void setSide(String side) {
		this.side = side;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public double getTileHeight() {
		return tileHeight;
	}

	public void setTileHeight(double tileHeight) {
		this.tileHeight = tileHeight;
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
