package it.l_soft.wediConf.rest.dbUtils;

import java.util.ArrayList;

import org.apache.log4j.Logger;

public class Trays extends DBInterface
{	
	private static final long serialVersionUID = -4849479160608801245L;
//	private final Logger log = Logger.getLogger(this.getClass());
	
	protected int idTrays;
	protected String trayType;
	protected String drainType;
	protected String articleNumber;
	protected String description;
	protected String drainPosition;
	protected int width;
	protected int length;
	protected int thickness;
	protected int widthMin;
	protected int lengthMin;
	protected double price;
	
	private void setNames()
	{
		tableName = "trays";
		idColName = "idTrays";
	}

	public Trays()
	{
		setNames();
	}

	public Trays(DBConnection conn, int id) throws Exception
	{
		getTray(conn, id);
	}

	public void getTray(DBConnection conn, int id) throws Exception
	{
		setNames();
		String sql = "SELECT * " +
					 "FROM " + tableName + " " +
					 "WHERE " + idColName + " = " + id;
		this.populateObject(conn, sql, this);
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<Trays> findArticles(DBConnection conn, String whereCondition, int languageCode) throws Exception {
		Logger log = Logger.getLogger(Trays.class);
		String sql = "SELECT * " +
				 "FROM trays " +
				 "WHERE " + whereCondition;
		log.trace("Querying: " + sql);
		return (ArrayList<Trays>) DBInterface.populateCollection(conn, sql, Trays.class);
	}

	public int getIdTrays() {
		return idTrays;
	}

	public void setIdTrays(int idTrays) {
		this.idTrays = idTrays;
	}

	public String getDrainType() {
		return drainType;
	}

	public void setDrainType(String drainType) {
		this.drainType = drainType;
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

	public String getDrainPosition() {
		return drainPosition;
	}

	public void setDrainPosition(String drainPosition) {
		this.drainPosition = drainPosition;
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

	public int getThickness() {
		return thickness;
	}

	public void setThickness(int thickness) {
		this.thickness = thickness;
	}

	public int getWidthMin() {
		return widthMin;
	}

	public void setWidthMin(int widthMin) {
		this.widthMin = widthMin;
	}

	public int getLengthMin() {
		return lengthMin;
	}

	public void setLengthMin(int lengthMin) {
		this.lengthMin = lengthMin;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}
}

