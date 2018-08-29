package hello;

import java.util.Collection;

import org.activiti.Applicant;
import org.activiti.ApplicantRepository;
import org.activiti.HireProcessRestController;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.util.StringUtils;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;

@Route
public class MainView extends VerticalLayout {
	private static final long serialVersionUID = 1L;

	private CustomerRepository customerRepo;
	
    @Autowired
	private ApplicantRepository applicantRepo;

	private final CustomerEditor editor;

	final Grid<Customer> customerGrid;
	final Grid<Applicant> applicantGrid;

	final TextField filter;

	private final Button addNewBtn;

    @Autowired
    private RuntimeService runtimeService;


	public MainView(CustomerRepository repo, CustomerEditor editor) {
		this.customerRepo = repo;
		this.editor = editor;
		this.customerGrid = new Grid<>(Customer.class);
		this.applicantGrid = new Grid<>(Applicant.class);
		this.filter = new TextField();
		this.addNewBtn = new Button("New customer", VaadinIcon.PLUS.create());

		// build layout
		HorizontalLayout actions = new HorizontalLayout(filter, addNewBtn);
		add(actions, customerGrid, applicantGrid, editor);

		customerGrid.setHeight("300px");
		customerGrid.setColumns("id", "firstName", "lastName");
		customerGrid.getColumnByKey("id").setWidth("50px").setFlexGrow(0);
		
		applicantGrid.setColumns("id", "name", "email", "phoneNumber");

		filter.setPlaceholder("Filter by last name of customer");

		// Hook logic to components

		// Replace listing with filtered content when user changes filter
		filter.setValueChangeMode(ValueChangeMode.EAGER);
		filter.addValueChangeListener(e -> listCustomers(e.getValue()));

		// Connect selected Customer to editor or hide if none is selected
		customerGrid.asSingleSelect().addValueChangeListener(e -> {
			editor.editCustomer(e.getValue());
		});

		// Instantiate and edit new Customer the new button is clicked
		addNewBtn.addClickListener(e -> editor.editCustomer(new Customer("", "")));

		// Listen changes made by the editor, refresh data from backend
		editor.setChangeHandler(() -> {
			editor.setVisible(false);
			listCustomers(filter.getValue());
		});
		runtimeService.addEventListener(new ActivitiEventListener() {

			@Override
			public void onEvent(ActivitiEvent event) {
				System.out.println("did i log something?");
				listApplicants(null);
			}

			@Override
			public boolean isFailOnException() {
				System.out.println("it focking failed");
				return false;
			}
				
		}, ActivitiEventType.PROCESS_STARTED);
		
		// Initialize listing
		listCustomers(null);
	}

	<T> void listSomething(JpaRepository<T,Long> repo, Grid<T> grid, Object filterText) {
		if (StringUtils.isEmpty(filterText)) {
			grid.setItems(repo.findAll());
		}
		else {//FIXME corregir caso Applicants
			grid.setItems((Collection<T>) customerRepo.findByLastNameStartsWithIgnoreCase(filterText.toString()));
		}
	}
	
	void listCustomers(String filterText) {
		this.listSomething(customerRepo, customerGrid, filterText);
	}
	
	private void listApplicants(Object object) {
		this.listSomething(applicantRepo, applicantGrid, object);
	}

}
