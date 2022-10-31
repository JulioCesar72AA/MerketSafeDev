package mx.softel.cirwireless.web_services_module.web_service.web_service_enums;

public enum WebServiceStatusExecutionCodes {
    USER_ALLOWED, // El usuario tiene permiso para iniciar sesion
    UNKNOWN_USER, // El usuario es desconocido
    CODES_NOT_MATCH, // Los codigos no coinciden
    USER_NOT_ALLOWED // El usuario no esta autorizado
}
