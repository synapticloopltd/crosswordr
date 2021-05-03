package synapticloop.puzzlr.exception;

/*
 * Copyright (c) 2019 - 2021 Synapticloop.
 * 
 * All rights reserved.
 * 
 * This code may contain contributions from other parties which, where 
 * applicable, will be listed in the default build file for the project 
 * ~and/or~ in a file named CONTRIBUTORS.txt in the root of the project.
 * 
 * This source code and any derived binaries are covered by the terms and 
 * conditions of the Licence agreement ("the Licence").  You may not use this 
 * source code or any derived binaries except in compliance with the Licence.  
 * A copy of the Licence is available in the file named LICENSE.txt shipped with 
 * this source code or binaries.
 */

public class PuzzlrException extends Exception {
	private static final long serialVersionUID = 6321463505225680651L;

	public PuzzlrException(String message) {
		super(message);
	}

	public PuzzlrException(String message, Exception exception) {
		super(message, exception);
	}
}
