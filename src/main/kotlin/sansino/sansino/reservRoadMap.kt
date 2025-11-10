
package sansino.sansino

/*
User → ReservationController.createTemporaryReservation
→ ReservationService.createTemporaryReservation
→ SlotTimeRepository.save
→ ReservationRepository.save
→ reservationWebSocketController.notifyReservationCreated
User → PaymentController.startPayment
→ ZarinPalService.requestPayment
→ TransActionService.initiatePayment
→ TransactionRepository.save
→ ReservationRepository.save (link Transaction)
→ Redirect user to ZarinPal URL
User → ZarinPal (pay)
ZarinPal → PaymentController.paymentCallback
→ TransActionService.handlePaymentCallback
→ TransactionRepository.save (SUCCESS/FAILED)
→ ReservationRepository.save (CONFIRMED/CANCELLED)
→ SlotTimeRepository.save (if cancelled)
→ reservationWebSocketController.notifyReservationConfirmed/Cancelled
*/
