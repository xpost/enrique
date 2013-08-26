/***************************************************************************************
 * No part of this program may be photocopied reproduced or translated to another program
 * language without prior written consent of Matias E. Iseas
 **************************************************************************************/
package com.sigepro.web.service.impl;



import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sigepro.web.dao.IncidenciaDAO;
import com.sigepro.web.dao.VisitaDAO;
import com.sigepro.web.dao.impl.DAOLocator;
import com.sigepro.web.model.dto.IncidenciaDTO;
import com.sigepro.web.model.dto.IncidenciaData;
import com.sigepro.web.model.dto.VisitaDTO;
import com.sigepro.web.model.pojo.Cliente;
import com.sigepro.web.model.pojo.EstadoInc;
import com.sigepro.web.model.pojo.EstadoVis;
import com.sigepro.web.model.pojo.Incidencia;
import com.sigepro.web.model.pojo.TipoInc;
import com.sigepro.web.model.pojo.Visita;
import com.sigepro.web.service.IncidenciaService;
import com.sigepro.web.transformer.Transformer;



/**
 * TODO comment<br>
 * <br>
 * <b>Change log:</b>
 * <ul>v
 * <li>1.0 20/02/2013 - Xpost - initial version</li>
 * </ul>
 * 
 * @author Xpost
 * @version 1.0
 */
@Transactional
@Service
public class IncidenciaServiceImpl implements IncidenciaService {
	
	
	Logger LOG = LoggerFactory.getLogger(IncidenciaServiceImpl.class);
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");

	@Autowired
	@Qualifier("visitaTransformer")
	Transformer<Visita, VisitaDTO> visitaTransformer;
	
    private IncidenciaDAO getIncidenciaDAO() {
        return DAOLocator.getInstance().lookup(IncidenciaDAO.class.getName());
    }
    private VisitaDAO getVisitaDAO() {
        return DAOLocator.getInstance().lookup(VisitaDAO.class.getName());
    }

    public Long saveIncidencia(IncidenciaData incidenciaData) throws ParseException {

    	IncidenciaDTO incidenciaDTO = incidenciaData.getIncidencia();
    	VisitaDTO visitaDTO = incidenciaData.getVisita();		
    	
        Incidencia incidencia =  new Incidencia();
        
        Cliente cliente = new Cliente();
        cliente.setIdcliente(incidenciaDTO.getIdcliente());
		incidencia.setCliente(cliente);
        incidencia.setDescripcion(incidenciaDTO.getDescripcion());
        EstadoInc estadoInc = new EstadoInc();
        estadoInc.setIdestadoinc(incidenciaDTO.getIdestadoinc());
		incidencia.setEstadoInc(estadoInc);
        
        TipoInc tipoInc = new TipoInc();
        tipoInc.setIdtipoincidencia(incidenciaDTO.getIdtipoincidencia());
		incidencia.setTipoInc(tipoInc);
		incidencia.setFechaInicio(dateFormatter.parse(incidenciaDTO.getFechaInicio()));
        incidencia.setTitulo(incidenciaDTO.getTitulo());
  
  
        Long incidenciaId =getIncidenciaDAO().saveIncidencia(incidencia);
        
        if (visitaDTO.getFechaInicia()!=null && !visitaDTO.getFechaInicia().isEmpty()){
	        Visita visita =  new Visita();
	        
	        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
	        Date inicialTime = null;
			try {
				inicialTime = df.parse(visitaDTO.getFechaInicia()+" "+visitaDTO.getHoraInicia());
			
	        if (visitaDTO.getDiacompleto()==null || !visitaDTO.getDiacompleto()){
		        Date endTime = df.parse(visitaDTO.getFechaTermina()+" "+visitaDTO.getHoraTermina());
		        visita.setTermina(endTime);
	        }
	        
			} catch (ParseException e) {
				Log.error(e.getMessage());
			}
	        visita.setDescripcion(incidenciaDTO.getDescripcion());
	        visita.setDiacompleto(visitaDTO.getDiacompleto());
	        visita.setInicia(inicialTime);
	        visita.setTitulo(visitaDTO.getTitulo());
			visita.setIncidencia(incidencia);      
			visita.setDireccion(visitaDTO.getDireccion());
			EstadoVis estadoVis = new EstadoVis();
			estadoVis.setIdestadovis(Integer.parseInt(visitaDTO.getEstadoId()));
			visita.setEstadoVis(estadoVis);
	        getVisitaDAO().saveVisita(visita);
        }
        
        return incidenciaId;

    }
    
    public Long updateIncidencia(IncidenciaData incidenciaData) throws ParseException {

    	IncidenciaDTO incidenciaDTO = incidenciaData.getIncidencia();
    	VisitaDTO visitaDTO = incidenciaData.getVisita();		
    	
        Incidencia incidencia = getIncidenciaDAO().loadIncidenciaById(incidenciaDTO.getIdincidencia());
        
        incidencia.setDescripcion(incidenciaDTO.getDescripcion());
        EstadoInc estadoInc = new EstadoInc();
        estadoInc.setIdestadoinc(incidenciaDTO.getIdestadoinc());
		incidencia.setEstadoInc(estadoInc);
        
        TipoInc tipoInc = new TipoInc();
        tipoInc.setIdtipoincidencia(incidenciaDTO.getIdtipoincidencia());
		incidencia.setTipoInc(tipoInc);
        incidencia.setTitulo(incidenciaDTO.getTitulo());
  
        Long incidenciaId =getIncidenciaDAO().saveIncidencia(incidencia);
        
        if ((visitaDTO.getFechaInicia()!=null && !visitaDTO.getFechaInicia().isEmpty()) || visitaDTO.getIdvisita()!=null ){
	       
        	Visita visita =  new Visita();
        	if (visitaDTO.getIdvisita()!=null && !visitaDTO.getIdvisita().isEmpty() ){
        		visita = getVisitaDAO().loadVisitaById(Long.parseLong(visitaDTO.getIdvisita()));
        	}
        	try {
        		Integer estadoIdV = Integer.parseInt(visitaDTO.getEstadoId());
        		if (estadoIdV>9){ //visita cancelada
        			EstadoVis estadoVis = new EstadoVis();
        			estadoVis.setIdestadovis(estadoIdV);
        			visita.setEstadoVis(estadoVis);
        			visita.setDescripcion(visitaDTO.getDescripcion());
        	        getVisitaDAO().saveVisita(visita);
        	        return incidenciaId;
        		}
			} catch (NumberFormatException e) {
				LOG.warn("El estado de la visita no se puedo obtener!");
			}
        	 
        	
	        
	        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
	        Date inicialTime = null;
			try {
				inicialTime = df.parse(visitaDTO.getFechaInicia()+" "+visitaDTO.getHoraInicia());
			
	        if (visitaDTO.getDiacompleto()==null || !visitaDTO.getDiacompleto()){
		        Date endTime = df.parse(visitaDTO.getFechaTermina()+" "+visitaDTO.getHoraTermina());
		        visita.setTermina(endTime);
	        }
	        
			} catch (ParseException e) {
				Log.error(e.getMessage());
			}
	        visita.setDescripcion(incidenciaDTO.getDescripcion());
	        visita.setDiacompleto(visitaDTO.getDiacompleto());
	        visita.setInicia(inicialTime);
	        visita.setTitulo(visitaDTO.getTitulo());
			visita.setIncidencia(incidencia);      
			visita.setDireccion(visitaDTO.getDireccion());
			EstadoVis estadoVis = new EstadoVis();
			estadoVis.setIdestadovis(Integer.parseInt(visitaDTO.getEstadoId()));
			visita.setEstadoVis(estadoVis);
	        getVisitaDAO().saveVisita(visita);
        }
        
        return incidenciaId;

    }

    public List<IncidenciaDTO> listIncidenciasByParameters(String clientId, String fechaIni, Integer typeIncidencia, Integer stateIncidencia, String fieldSort, String sort) {
    	  List<IncidenciaDTO> resultList = new ArrayList<IncidenciaDTO>();
    	  try {
    	      DateFormat df = new SimpleDateFormat("dd/MM/yyyy");

    	      Date inicialTime = null;
//    	      Date endTime = null;
    	      Integer clientIdNum = null;
    			try {
    				inicialTime = df.parse(fechaIni);
//    		        endTime = df.parse(fechaTer+" "+timeTer);      
    			} catch (ParseException e) {
    				LOG.error(e.getMessage());
    			}
    			if (clientId!=null && !clientId.isEmpty()){
    				 clientIdNum = Integer.parseInt(clientId);
    			}
    	  	resultList= getIncidenciaDAO().getIncidenciasByParameters(inicialTime, clientIdNum, typeIncidencia, stateIncidencia, fieldSort, sort);
    	  } catch (Exception e) {
    	      e.printStackTrace();
    	  }

    	  return resultList;

    	}
    
    
    public void deleteIncidencia(Long incidenciaId) {
        Incidencia incidencia = new Incidencia();
        incidencia.setIdincidencia(incidenciaId);
        Incidencia delIncidencia = getIncidenciaDAO().loadIncidenciaById(incidenciaId);
        for (Visita visita : delIncidencia.getVisitas()) {
			getVisitaDAO().deleteVisita(visita);
		} 
        getIncidenciaDAO().deleteIncidencia(delIncidencia);

    }
    
	public Map<String,Object> loadAllAddIncidenciaCat(){
		LOG.info("loadAllAddIncidenciaCat");
		Map<String,Object> mapElements = new HashMap<String, Object>();
        try {
        	List<EstadoInc> estadoInc = getIncidenciaDAO().loadAllEstadoIncidencia();
        	List<TipoInc> tipoInc = getIncidenciaDAO().loadAllTipoIncidencia();
        	List<EstadoVis> estadoVisita = getVisitaDAO().loadAllEstadoVisita();
        	mapElements.put("estadoI", estadoInc);
        	mapElements.put("tipoI", tipoInc);
        	mapElements.put("estadoV", estadoVisita);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mapElements;

	}
	
	
	public VisitaDTO loadVisitDTOByIncidencia(Long incidenciaId) {
		VisitaDTO visitaDTO = new VisitaDTO();

		try {
			Incidencia incidencia = getIncidenciaDAO().loadIncidenciaById(incidenciaId);
			//TODO
			for (Visita visita : incidencia.getVisitas()) {
				if (visita.getEstadoVis().getIdestadovis()==0){
					visitaDTO = visitaTransformer.transform(visita);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("Ninguna visita fue asignada aun o no encontrada");
		}

		return visitaDTO;
	}

//	public Integer loadClientLastId(){
//		Integer lastId= -1;
//        try {
//        	lastId = getClientDAO().loadLastClientId();
//			
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return lastId;
//
//	}
//	

	
//	public ReportData loadCPForReport(Integer ocId){
//        ReportData reportData = null;
//		try {
//			Cliente client = getClientDAO().loadClientById(ocId);
//        	reportData = createReportData(client);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return reportData;
//	}
	
//	private ReportData createReportData(Cliente client) {
//		ReportData reportData = new ReportData();
//		ClienteDTO clientDTO = clientTransformer.transform(client);
//		reportData.setClient(clientDTO);
//		if (presup.getFechaentrega()!=null){
//		reportData.setDateDelivery(dateFormatter.format(presup.getFechaentrega()));
//		}
//		reportData.setDateReport(dateFormatter.format(presup.getFechacompra()));
//		reportData.setPayCondition(presup.getCondcompra());
//		DecimalFormat df = new DecimalFormat("##.00");
//		reportData.setTotalPriceOrder(df.format(presup.getPreciototal()));
//		reportData.setNumPresupuesto(presup.getNrocompra().toString());
		
//		List<ProductReportDTO> productReportDTOs = new ArrayList<ProductReportDTO>();
//		for (ProductoOc product : presup.getProductoOcs()){
//			productReportDTOs.add(productOCTransformer.transform(product));
//		}
//		reportData.setItems(productReportDTOs);
//		return reportData;
//	}


}
