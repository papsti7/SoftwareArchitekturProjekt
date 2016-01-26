/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sa_frontend;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author alex
 */
class CustomHandler implements ActionListener {

        private DefaultTableModel station_grid;
        private Object[] entries;
        
        public CustomHandler(DefaultTableModel station_grid, Object[] entries) {
            this.station_grid = station_grid;
            this.entries = entries;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            int id = Integer.parseInt(e.getActionCommand());
            station_grid.addRow(entries);
        }
    }
