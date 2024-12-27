package edu.uclm.esi.fakeaccountsbe.http;

import edu.uclm.esi.fakeaccountsbe.services.PagosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("pagos")
@CrossOrigin(origins = "*", allowedHeaders = "*")

public class PagosController {

	@Autowired
	private PagosService service;

	// Pregunta examen como pasa el flujo sin que sepamos los datos de la tarjeta en
	// el backend?
	@PutMapping("/prepararTransaccion")
	public String prepararTransaccion(@RequestBody float importe) {
		System.out.println("Importe: " + importe * 100);
		return this.service.prepararTransaccion((long) (importe * 100));
	}


	@GetMapping("/stripeKey")
	public String getStripeKey() {
		return this.service.getStripeKey();
	}
}
