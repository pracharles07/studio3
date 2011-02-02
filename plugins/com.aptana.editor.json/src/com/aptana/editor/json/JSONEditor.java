/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.json;

import org.eclipse.jface.text.IDocument;

import com.aptana.editor.common.AbstractThemeableEditor;
import com.aptana.editor.common.outline.CommonOutlinePage;
import com.aptana.editor.common.parsing.FileService;
import com.aptana.editor.common.text.reconciler.IFoldingComputer;
import com.aptana.editor.json.internal.text.JSONFoldingComputer;
import com.aptana.editor.json.outline.JSONOutlineContentProvider;
import com.aptana.editor.json.outline.JSONOutlineLabelProvider;
import com.aptana.editor.json.parsing.IJSONParserConstants;

public class JSONEditor extends AbstractThemeableEditor
{
	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.AbstractThemeableEditor#createFileService()
	 */
	@Override
	protected FileService createFileService()
	{
		return new FileService(IJSONParserConstants.LANGUAGE);
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.AbstractThemeableEditor#createOutlinePage()
	 */
	@Override
	protected CommonOutlinePage createOutlinePage()
	{
		CommonOutlinePage outline = super.createOutlinePage();

		outline.setContentProvider(new JSONOutlineContentProvider());
		outline.setLabelProvider(new JSONOutlineLabelProvider());

		return outline;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.AbstractThemeableEditor#initializeEditor()
	 */
	protected void initializeEditor()
	{
		super.initializeEditor();

		this.setSourceViewerConfiguration(new JSONSourceViewerConfiguration(this.getPreferenceStore(), this));
		this.setDocumentProvider(new JSONDocumentProvider());
	}
	
	@Override
	public IFoldingComputer createFoldingComputer(IDocument document)
	{
		return new JSONFoldingComputer(this, document);
	}
}