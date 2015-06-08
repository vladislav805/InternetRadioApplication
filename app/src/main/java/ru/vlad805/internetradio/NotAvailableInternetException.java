package ru.vlad805.internetradio;

import android.content.Context;

import java.io.IOException;

public class NotAvailableInternetException extends IOException {
	private static final long serialVersionUID = 1L;
	public NotAvailableInternetException () {
		super();
	}
}