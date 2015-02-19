package controller;


import facade.PessoaFacade;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import model.Pessoa;

import util.PessoaLazyModel;

@Named(value = "pessoaController")
@SessionScoped
public class PessoaController implements Serializable{
    
    public PessoaController() {
        pessoa = new Pessoa();
        cadastro = new CadastroBean();
    }

    //Guarda o pessoaatual
    private Pessoa pessoa;
    
    private CadastroBean cadastro;

    @EJB
    private PessoaFacade pessoaFacade;
    private PessoaLazyModel pessoaDataModel;
    

    public void setPessoa(Pessoa pessoa) {
        this.pessoa = pessoa;
    }

    private Pessoa getPessoa(Long key) {
        return this.buscar(key);

    }

    public Pessoa getPessoa() {
        if (pessoa== null) {
            pessoa= new Pessoa();
        }
        return pessoa;
    }


    //---------------------------------------Páginas web------------------------------------------------------------
    public String prepareCreate(int i) {
        pessoa= new Pessoa();
        if (i == 1) {
            return "/view/pessoa/Create";
        } else {
            return "Create";
        }
    }

    public String index() {
        pessoa= null;
        pessoaDataModel = null;
        return "/index";
    }

    public String prepareEdit() {
        pessoa= (Pessoa) pessoaDataModel.getRowData();
        return "Edit";
    }

    public String prepareView() {
        pessoa= (Pessoa) pessoaDataModel.getRowData();
        //pessoa= pessoaFacade.find(pessoa.getID());
        //pessoaFacade.edit(pessoaFacade.find(pessoa.getID()));
        //pessoaFacade.edit(pessoa);
        return "View";
    }
    
    //---------------------------LazyData Model--------------------------------------------------------------------
    
    @PostConstruct
    public void init() {
        pessoaDataModel = new PessoaLazyModel(this.listarTodas());
    }
    
    public PessoaLazyModel getPessoaLazyModel() {
        
        if(pessoaDataModel == null){
            pessoaDataModel = new PessoaLazyModel(this.listarTodas());
        }
        
        
        return this.pessoaDataModel;
    }
    
    
//    public void preencherDataModel(){
//        
//        cadastro.cadastrarPessoas();
//        pessoaDataModel = null;
//        
//    }
    
    //---------------------------------------------------CRUD-------------------------------------------------------
    private List<Pessoa> listarTodas() {
        return pessoaFacade.findAll();

    }

    
    public void salvarNoBanco() {

        try {
            pessoaFacade.save(pessoa);
            JsfUtil.addSuccessMessage("Pessoa " + pessoa.getNome() + " criado com sucesso!");
            pessoa= null;
            recriarModelo();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, "Ocorreu um erro de persistência");

        }

    }

    public Pessoa buscar(Long id) {

        return pessoaFacade.find(id);
    }

    public void editar() {
        try {
            pessoaFacade.edit(pessoa);
            JsfUtil.addSuccessMessage("Pessoa Editado com sucesso!");
            pessoa= null;
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, "Ocorreu um erro de persistência, não foi possível editar o pessoa: " + e.getMessage());

        }
    }

    public void delete() {
        pessoa= (Pessoa) pessoaDataModel.getRowData();
        try {
            pessoaFacade.remove(pessoa);
            pessoa= null;
            JsfUtil.addSuccessMessage("Pessoa Deletado");
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, "Ocorreu um erro de persistência");
        }

        recriarModelo();
    }
    
    public SelectItem[] getItemsAvaiableSelectOne() {
        return JsfUtil.getSelectItems(pessoaFacade.findAll(), true);
    }

    //--------------------------------------------------------------------------------------------------------------

    public void recriarModelo() {
    
        pessoaDataModel = null;

    }
    
    
    //Cadastro-------------------------------------------------------------------------------------------
    
    
    public void cadastrarPessoas() {

        String[] palavras;

        try {

            FileReader arq = new FileReader("/home/charles/NetBeansProjects/Arquivos CSV/docentes.csv");

            BufferedReader lerArq = new BufferedReader(arq);

            String linha = lerArq.readLine(); //cabeçalho
            // lê a primeira linha 
            // a variável "linha" recebe o valor "null" quando o processo 
            // de repetição atingir o final do arquivo texto 

            linha = lerArq.readLine();
            
//            linha = linha.replaceAll("\"", "");

            while (linha != null) {
                
                linha = linha.replaceAll("\"", "");

                palavras = linha.split(",");

                List<Pessoa> pessoas = pessoaFacade.findByName(trataNome(palavras[1]));
                
                if (pessoas.isEmpty()) {

                    Pessoa p = new Pessoa();

                    p.setNome(trataNome(palavras[1]));
                    p.setSiape(palavras[2]);
                    p.setEmail(palavras[3]);
                    p.setCentro(palavras[4]);

                    pessoaFacade.save(p);

                }

                linha = lerArq.readLine();
//                linha = linha.replaceAll("\"", "");
            }

            arq.close();

        } catch (IOException e) {
            System.err.printf("Erro na abertura do arquivo: %s.\n", e.getMessage());
        }

        recriarModelo();

    }
    
    
    private String trataNome(String nome) { 
        
     String retorno = "";
     String[] palavras = nome.split(" ");
     
     for(String p: palavras){
         
         if(p.equals("DAS") || p.equals("DOS") || p.length() <= 2){
             p = p.toLowerCase();
             retorno += p + " ";
         }
        
         
         else{
             p = p.charAt(0) + p.substring(1, p.length()).toLowerCase();
             retorno += p + " ";
         }
         
     }
        
return retorno;

} 
    
    //AutoComplete----------------------------------------------------------------------------------------
    public List<Pessoa> completePessoa(String query) {
        
       query = query.toLowerCase();
        
        List<Pessoa> allPessoas = this.listarTodas();
        List<Pessoa> filteredPessoas = new ArrayList<>();

        for (Pessoa p : allPessoas) {
            if (p.getNome().toLowerCase().startsWith(query)) {
                filteredPessoas.add(p);
            }
        }
        return filteredPessoas;
    }

//    public List<Disciplina> completeDisciplina(String query) {
//        List<Disciplina> allDisciplinas = this.listarTodas();
//        List<Disciplina> filteredDisciplinas = new ArrayList<>();
//
//        for (int i = 0; i < allDisciplinas.size(); i++) {
//            Disciplina d = allDisciplinas.get(i);
//            if (d.getNome().toLowerCase().contains(query)) {
//                filteredDisciplinas.add(d);
//            }
//        }
//
//        return filteredDisciplinas;
//    }

    //----------------------------------------------------------------------------------------------------

    
    
    //---------------------------------------------------------------------------------------------------
    

    @FacesConverter(forClass = Pessoa.class)
    public static class PessoaControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            PessoaController controller = (PessoaController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "pessoaController");
            return controller.getPessoa(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            key = Long.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Pessoa) {
                Pessoa d = (Pessoa) object;               
                return getStringKey(new BigDecimal(d.getID().toString()).setScale(0, BigDecimal.ROUND_HALF_UP).longValue());

            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Pessoa.class.getName());
            }
        }
    }

}