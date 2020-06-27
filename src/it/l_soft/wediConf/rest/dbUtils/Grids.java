package it.l_soft.wediConf.rest.dbUtils;

import java.util.ArrayList;

import org.apache.log4j.Logger;

public class Grids extends DBInterface
{	
	private static final long serialVersionUID = -4849479160608801245L;
//	private final Logger log = Logger.getLogger(this.getClass());
	
	protected int idGrids;
	protected String articleNumber;
	protected String trayType;
	protected String description;
	protected int length;
	protected int width;
	protected double thickness;
	protected double price;
	protected boolean selected = false;
	
	private void setNames()
	{
		tableName = "grids";
		idColName = "idGrids";
	}

	public Grids()
	{
		setNames();
	}

	public Grids(DBConnection conn, int id) throws Exception
	{
		getGrids(conn, id);
	}

	public void getGrids(DBConnection conn, int id) throws Exception
	{
		setNames();
		String sql = "SELECT * " +
					 "FROM " + tableName + " " +
					 "WHERE " + idColName + " = " + id;
		this.populateObject(conn, sql, this);
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<Grids> getGrids(DBConnection conn, String whereCondition, int languageCode) throws Exception {
		Logger log = Logger.getLogger(Grids.class);
		String sql = "SELECT * " +
				 "FROM grids " +
				 "WHERE " + whereCondition;
		log.trace("Querying: " + sql);
		return (ArrayList<Grids>) DBInterface.populateCollection(conn, sql, Grids.class);
	}


	public int getIdGrids() {
		return idGrids;
	}

	public void setIdGrids(int idGrids) {
		this.idGrids = idGrids;
	}

	public String getTrayType() {
		return trayType;
	}

	public void setTrayType(String trayType) {
		this.trayType = trayType;
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

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public double getThickness() {
		return thickness;
	}

	public void setThickness(double thickness) {
		this.thickness = thickness;
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
