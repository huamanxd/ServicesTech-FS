function Piedepagina(){
    return(
        <div className=" mx-auto w-full border-t border-gray-200 py-4">

            <div className="max-w-7xl mx-auto grid grid-cols-1 md:grid-cols-3 gap-8 md:gap-16 align-center items-start py-3">
                <div>
                    <h1 className="font-bold" >Contacto</h1>
                    <p>ServicesTech S.A.C.</p>
                    <p>Lima, Perú</p>
                    <p>Tel: <span className="text-blue-500"  >+51 928 536 863</span></p>
                    <p>Correo: <span className="text-blue-500" >hchristian6@live.com</span></p>
                </div>
                <div>
                    <h1 className="font-bold" >Ubicación / Mapa</h1>
                    <p>A.v. Canada 1804</p>
                    <div className="w-full max-w-sm h-32 rounded-md overflow-hidden border border-gray-300 mt-2">
                        <iframe
                            src="https://www.google.com/maps?q=Av.+Canada+1804,+Lima,+Peru&output=embed"
                            className="w-full h-full"
                            loading="lazy"
                            title="Mapa de ubicación"
                        ></iframe>
                    </div>
                </div>
                <div className="mx-auto" >
                    <h1 className="font-bold" >Legal</h1>
                    <p className="text-blue-500 hover:cursor-pointer hover:underline">Política de Privacidad</p>
                    <p className="text-blue-500 hover:cursor-pointer hover:underline">Libro de reclamaciones</p>
                </div>
            </div>
            <div className="container mx-auto  h-4 border-b border-gray-200 py-0">
            </div>
            <p className="container mx-auto py-6 text-center text-sm text-gray-500">Todos los derechos reservados © Services Tech</p>
        </div>
    );
}
export default Piedepagina;