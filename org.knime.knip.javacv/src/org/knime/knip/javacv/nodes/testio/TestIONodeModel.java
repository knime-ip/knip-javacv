/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2013
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * Created on 11.03.2013 by dietyc
 */
package org.knime.knip.javacv.nodes.testio;

import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;

import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.meta.ImgPlus;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.knip.base.data.img.ImgPlusCell;
import org.knime.knip.base.data.img.ImgPlusCellFactory;

/**
 * @author dietzc, University of Konstanz
 */
public class TestIONodeModel extends NodeModel {

	private ImgPlusCellFactory m_imgPlusFactory;

	protected TestIONodeModel() {
		super(0, 1);
	}

	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
			throws InvalidSettingsException {
		return new DataTableSpec[] { createOutSpec() };
	}

	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {

		m_imgPlusFactory = new ImgPlusCellFactory(exec);

		final BufferedDataContainer container = exec
				.createDataContainer(createOutSpec());

		final String path = "/home/dietzc/dietzc85@googlemail.com/projects/Leuven/2014-02-10 - 5dpf ctrl uninjected AB_c1_0001.avi";
		int timeIdx = 0;

		final FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(path);

		grabber.start();

		try {
			while (true) {

				if (timeIdx % 15 == 0) {
					createImgPlusAndAddToContainer(((DataBufferByte) grabber
							.grab().getBufferedImage().getRaster()
							.getDataBuffer()).getData(), container, timeIdx);
				}
				grabber.grab();
				timeIdx++;
			}
		} catch (final Exception e) {
			e.printStackTrace();
		} finally {
			grabber.stop();
		}

		container.close();

		return new BufferedDataTable[] { container.getTable() };
	}

	private void createImgPlusAndAddToContainer(final byte[] buffer,
			final BufferedDataContainer container, final int idx)
			throws IOException {

		final int width = 1024;
		final int height = 768;

		final ArrayImg<UnsignedByteType, ?> img = new ArrayImgFactory<UnsignedByteType>()
				.create(new long[] { width, height }, new UnsignedByteType());

		final byte[] update = ((ByteArray) img.update(null))
				.getCurrentStorageArray();

		for (int i = 0; i < update.length; i++) {
			update[i] = buffer[i * 3];
		}

		container.addRowToTable(new DefaultRow("" + idx, m_imgPlusFactory
				.createCell(new ImgPlus<>(img))));

	}

	private DataTableSpec createOutSpec() {
		return new DataTableSpec(new DataColumnSpecCreator("Image",
				ImgPlusCell.TYPE).createSpec());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
		// Nothing to do since now
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File nodeInternDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
		// Nothing to do here
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File nodeInternDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
		// Nothing to do here
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
	}

}