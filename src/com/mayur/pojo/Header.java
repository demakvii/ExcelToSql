package com.mayur.pojo;

public class Header {

	private String headerName;
	private int position;

	public String getHeaderName() {
		return headerName;
	}

	public Header(int position, String headerName) {
		this.headerName = headerName;
		this.position = position;
	}

	public void setHeaderName(String headerName) {
		this.headerName = headerName;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public Header getByPosition(int position) {
		if (this.position == position)
			return this;
		else
			return null;
	}

	public Header getByHeaderName(String headerName) {
		if (this.headerName.equals(headerName))
			return this;
		else
			return null;
	}

}
