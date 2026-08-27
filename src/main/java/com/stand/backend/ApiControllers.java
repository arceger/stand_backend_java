package com.stand.backend;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Marketplace Público", description = "Endpoints abertos ao público para consulta de veículos e envio de propostas/leads")
@RestController
@RequestMapping("/api/public")
class PublicApiController {
    private final MarketplaceService marketplaceService;

    PublicApiController(MarketplaceService marketplaceService) {
        this.marketplaceService = marketplaceService;
    }

    @Operation(
        summary = "Listar veículos do catálogo público",
        description = "Retorna lista de veículos publicados, com suporte a filtros opcionais por busca textual, marca, ano mínimo e preço máximo."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de veículos recuperada com sucesso",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = VehicleCardResponse.class))))
    })
    @GetMapping("/vehicles")
    List<VehicleCardResponse> listVehicles(
        @Parameter(description = "Busca textual por título, marca ou modelo", example = "Civic")
        @RequestParam(required = false) String search,
        @Parameter(description = "Marca do veículo", example = "Honda")
        @RequestParam(required = false) String brand,
        @Parameter(description = "Ano de fabricação mínimo", example = "2020")
        @RequestParam(required = false) Integer minYear,
        @Parameter(description = "Preço máximo", example = "150000.00")
        @RequestParam(required = false) BigDecimal maxPrice
    ) {
        return marketplaceService.listPublished(new VehicleFilter(search, brand, minYear, maxPrice, null, false))
            .stream()
            .map(VehicleMapper::card)
            .toList();
    }

    @Operation(
        summary = "Listar veículos em destaque",
        description = "Retorna os veículos marcados como destaque (featured) para a vitrine principal da home."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de veículos em destaque recuperada com sucesso",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = VehicleCardResponse.class))))
    })
    @GetMapping("/vehicles/featured")
    List<VehicleCardResponse> featuredVehicles() {
        return marketplaceService.featuredVehicles().stream().map(VehicleMapper::card).toList();
    }

    @Operation(
        summary = "Obter detalhes de um veículo por slug",
        description = "Retorna a ficha técnica completa e a galeria de imagens do veículo através do seu slug amigável."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Detalhes do veículo recuperados com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = VehicleDetailResponse.class))),
        @ApiResponse(responseCode = "404", description = "Veículo não encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/vehicles/{slug}")
    @Transactional(readOnly = true)
    VehicleDetailResponse vehicleDetail(
        @Parameter(description = "Slug amigável do veículo", example = "honda-civic-2022-exl")
        @PathVariable String slug
    ) {
        return VehicleMapper.detail(marketplaceService.getPublishedBySlug(slug));
    }

    @Operation(
        summary = "Enviar proposta / lead de interesse",
        description = "Registra os dados de contato e mensagem de interesse de um cliente para o veículo especificado."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Lead registrado com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados da proposta inválidos ou campos obrigatórios ausentes",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Veículo não encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/vehicles/{slug}/leads")
    ResponseEntity<MessageResponse> createLead(
        @Parameter(description = "Slug amigável do veículo", example = "honda-civic-2022-exl")
        @PathVariable String slug,
        @RequestBody LeadFormRequest request
    ) {
        marketplaceService.createLead(slug, new LeadRequest(
            request.customerName(),
            request.phone(),
            request.email(),
            request.message()
        ));
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new MessageResponse("Interesse enviado com sucesso. A loja entrara em contato em breve."));
    }
}

@Tag(name = "Painel Administrativo", description = "Endpoints de gerenciamento do estoque, autenticação e upload de imagens")
@RestController
@RequestMapping("/api/admin")
class AdminApiController {
    private final AuthService authService;
    private final MarketplaceService marketplaceService;

    AdminApiController(AuthService authService, MarketplaceService marketplaceService) {
        this.authService = authService;
        this.marketplaceService = marketplaceService;
    }

    @Operation(
        summary = "Autenticação administrativa (Login)",
        description = "Autentica um administrador com email e senha, retornando os dados do usuário e um token Bearer para as demais rotas."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Autenticado com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "400", description = "Credenciais inválidas",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/auth/login")
    AuthResponse login(@RequestBody LoginRequest request) {
        return authService.authenticate(request.email(), request.password());
    }

    @Operation(
        summary = "Listar veículos do painel administrativo",
        description = "Retorna o resumo do estoque (total, destacados, publicados) e a listagem de todos os veículos com filtros opcionais por busca textual ou status.",
        security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH_SCHEME)
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dashboard e listagem retornados com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AdminDashboardResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado ou token inválido",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/vehicles")
    AdminDashboardResponse listVehicles(
        @Parameter(description = "Busca por título, marca ou modelo", example = "Corolla")
        @RequestParam(required = false) String search,
        @Parameter(description = "Filtrar por status do veículo")
        @RequestParam(required = false) VehicleStatus status
    ) {
        List<Vehicle> vehicles = marketplaceService.listAdmin(new VehicleFilter(search, null, null, null, status, false));
        return new AdminDashboardResponse(
            vehicles.size(),
            vehicles.stream().filter(Vehicle::isFeatured).count(),
            vehicles.stream().filter(vehicle -> vehicle.getStatus() == VehicleStatus.PUBLISHED).count(),
            vehicles.stream().map(VehicleMapper::admin).toList()
        );
    }

    @Operation(
        summary = "Obter dados completos de um veículo para edição",
        description = "Retorna todos os campos e a galeria de imagens de um veículo a partir de seu UUID.",
        security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH_SCHEME)
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Detalhes do veículo recuperados",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = VehicleDetailResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado ou token inválido",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Veículo não encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/vehicles/{id}")
    VehicleDetailResponse getVehicle(
        @Parameter(description = "UUID do veículo", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @PathVariable UUID id
    ) {
        return VehicleMapper.detail(marketplaceService.getAdminVehicle(id));
    }

    @Operation(
        summary = "Cadastrar novo veículo",
        description = "Cria um novo registro de veículo no estoque com os dados informados.",
        security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH_SCHEME)
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Veículo cadastrado com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = VehicleDetailResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado ou token inválido",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/vehicles")
    ResponseEntity<VehicleDetailResponse> createVehicle(@RequestBody VehicleFormRequest request) {
        Vehicle vehicle = marketplaceService.createVehicle(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).body(VehicleMapper.detail(vehicle));
    }

    @Operation(
        summary = "Atualizar veículo existente",
        description = "Atualiza todas as informações cadastrais do veículo identificado pelo UUID.",
        security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH_SCHEME)
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Veículo atualizado com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = VehicleDetailResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado ou token inválido",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Veículo não encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/vehicles/{id}")
    VehicleDetailResponse updateVehicle(
        @Parameter(description = "UUID do veículo", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @PathVariable UUID id,
        @RequestBody VehicleFormRequest request
    ) {
        return VehicleMapper.detail(marketplaceService.updateVehicle(id, request.toCommand()));
    }

    @Operation(
        summary = "Atualizar status de publicação do veículo",
        description = "Modifica apenas o status de publicação do veículo (DRAFT, PUBLISHED, SOLD, ARCHIVED).",
        security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH_SCHEME)
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = VehicleDetailResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado ou token inválido",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Veículo não encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/vehicles/{id}/status")
    VehicleDetailResponse updateStatus(
        @Parameter(description = "UUID do veículo", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @PathVariable UUID id,
        @RequestBody StatusUpdateRequest request
    ) {
        return VehicleMapper.detail(marketplaceService.updateStatus(id, request.status()));
    }

    @Operation(
        summary = "Fazer upload de imagens do veículo",
        description = "Envia um ou mais arquivos de imagem para inclusão na galeria do veículo.",
        security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH_SCHEME)
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Imagens enviadas e galeria atualizada retornada",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ImageResponse.class)))),
        @ApiResponse(responseCode = "400", description = "Nenhum arquivo enviado ou formato inválido",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado ou token inválido",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Veículo não encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/vehicles/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    List<ImageResponse> uploadImages(
        @Parameter(description = "UUID do veículo", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @PathVariable UUID id,
        @Parameter(description = "Arquivos de imagem para upload (JPEG/PNG/WEBP)")
        @RequestPart("files") List<MultipartFile> files
    ) {
        return marketplaceService.uploadImages(id, files).stream().map(VehicleMapper::image).toList();
    }

    @Operation(
        summary = "Substituir imagem específica do veículo",
        description = "Substitui o arquivo de uma imagem existente na galeria pelo novo arquivo enviado.",
        security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH_SCHEME)
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Imagem substituída e galeria atualizada retornada",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ImageResponse.class)))),
        @ApiResponse(responseCode = "400", description = "Arquivo inválido",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado ou token inválido",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Veículo ou imagem não encontrados",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping(value = "/vehicles/{id}/images/{imageId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    List<ImageResponse> replaceImage(
        @Parameter(description = "UUID do veículo", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @PathVariable UUID id,
        @Parameter(description = "UUID da imagem a ser substituída", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @PathVariable UUID imageId,
        @Parameter(description = "Novo arquivo de imagem")
        @RequestPart("file") MultipartFile file
    ) {
        return marketplaceService.replaceImage(id, imageId, file).stream().map(VehicleMapper::image).toList();
    }

    @Operation(
        summary = "Reordenar imagens da galeria",
        description = "Atualiza a ordem de exibição das imagens do veículo conforme a sequência de IDs informada.",
        security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH_SCHEME)
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ordem das imagens atualizada com sucesso",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ImageResponse.class)))),
        @ApiResponse(responseCode = "401", description = "Não autenticado ou token inválido",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Veículo não encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/vehicles/{id}/images/order")
    List<ImageResponse> reorderImages(
        @Parameter(description = "UUID do veículo", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @PathVariable UUID id,
        @RequestBody ImageOrderRequest request
    ) {
        return marketplaceService.reorderImages(id, request.imageIds()).stream().map(VehicleMapper::image).toList();
    }

    @Operation(
        summary = "Definir imagem como capa principal",
        description = "Marca a imagem especificada como foto de capa principal do veículo.",
        security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH_SCHEME)
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Capa definida com sucesso",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ImageResponse.class)))),
        @ApiResponse(responseCode = "401", description = "Não autenticado ou token inválido",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Veículo ou imagem não encontrados",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/vehicles/{id}/images/{imageId}/cover")
    List<ImageResponse> setCover(
        @Parameter(description = "UUID do veículo", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @PathVariable UUID id,
        @Parameter(description = "UUID da imagem que se tornará capa", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @PathVariable UUID imageId
    ) {
        return marketplaceService.setCoverImage(id, imageId).stream().map(VehicleMapper::image).toList();
    }

    @Operation(
        summary = "Excluir imagem da galeria",
        description = "Remove permanentemente a foto da galeria e apaga o arquivo físico no servidor.",
        security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH_SCHEME)
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Imagem removida com sucesso",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ImageResponse.class)))),
        @ApiResponse(responseCode = "401", description = "Não autenticado ou token inválido",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Veículo ou imagem não encontrados",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/vehicles/{id}/images/{imageId}")
    List<ImageResponse> deleteImage(
        @Parameter(description = "UUID do veículo", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @PathVariable UUID id,
        @Parameter(description = "UUID da imagem a ser removida", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @PathVariable UUID imageId
    ) {
        return marketplaceService.deleteImage(id, imageId).stream().map(VehicleMapper::image).toList();
    }
}


@Schema(description = "Credenciais para login administrativo")
record LoginRequest(
    @Schema(description = "E-mail cadastrado do administrador", example = "admin@stand.local")
    String email,
    @Schema(description = "Senha de acesso", example = "Admin123!")
    String password
) {
}

@Schema(description = "Formulário de envio de proposta/lead de interesse pelo cliente")
record LeadFormRequest(
    @Schema(description = "Nome completo do cliente interessado", example = "Carlos Silva")
    String customerName,
    @Schema(description = "Telefone ou WhatsApp de contato com DDD", example = "(11) 98765-4321")
    String phone,
    @Schema(description = "E-mail de contato", example = "carlos.silva@email.com")
    String email,
    @Schema(description = "Mensagem ou proposta enviada pelo cliente", example = "Olá, tenho interesse neste veículo. Gostaria de agendar uma visita.")
    String message
) {
}

@Schema(description = "Requisição para atualização de status de publicação do veículo")
record StatusUpdateRequest(
    @Schema(description = "Novo status do veículo (DRAFT, PUBLISHED, SOLD, ARCHIVED)", example = "PUBLISHED")
    VehicleStatus status
) {
}

@Schema(description = "Requisição para reordenação de imagens na galeria")
record ImageOrderRequest(
    @Schema(description = "Lista ordenada contendo os UUIDs das imagens")
    List<UUID> imageIds
) {
}

@Schema(description = "Resposta padrão de confirmação de mensagem")
record MessageResponse(
    @Schema(description = "Mensagem descritiva da operação", example = "Interesse enviado com sucesso. A loja entrara em contato em breve.")
    String message
) {
}

@Schema(description = "Métricas do dashboard e listagem administrativa de veículos")
record AdminDashboardResponse(
    @Schema(description = "Total de veículos cadastrados no sistema", example = "15")
    long total,
    @Schema(description = "Total de veículos em destaque na vitrine", example = "4")
    long highlighted,
    @Schema(description = "Total de veículos atualmente publicados", example = "10")
    long published,
    @Schema(description = "Lista de veículos cadastrados")
    List<AdminVehicleResponse> vehicles
) {
}

@Schema(description = "Card resumido de veículo para vitrine e listagem pública")
record VehicleCardResponse(
    @Schema(description = "ID único (UUID) do veículo", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID id,
    @Schema(description = "Slug amigável para URLs", example = "honda-civic-2022-exl")
    String slug,
    @Schema(description = "Título do anúncio", example = "Honda Civic 2.0 EXL Automático")
    String title,
    @Schema(description = "Marca do veículo", example = "Honda")
    String brand,
    @Schema(description = "Modelo do veículo", example = "Civic")
    String model,
    @Schema(description = "Ano de fabricação", example = "2022")
    Integer year,
    @Schema(description = "Ano do modelo", example = "2022")
    Integer modelYear,
    @Schema(description = "Quilometragem rodada", example = "35000")
    Integer mileage,
    @Schema(description = "Tipo de transmissão", example = "AUTOMATIC")
    String transmission,
    @Schema(description = "Tipo de combustível", example = "FLEX")
    String fuelType,
    @Schema(description = "Preço formatado em EUR (€)", example = "139.900,00 €")
    String price,
    @Schema(description = "Preço numérico exato", example = "139900.00")
    String rawPrice,
    @Schema(description = "URL da imagem de capa", example = "/uploads/civic-cover.jpg")
    String coverImageUrl,
    @Schema(description = "Indica se o anúncio está destacado", example = "true")
    boolean featured,
    @Schema(description = "Status do veículo", example = "PUBLISHED")
    String status,
    @Schema(description = "Destaques e opcionais principais", example = "Bancos em couro, Teto solar, Painel digital")
    String highlights
) {
}

@Schema(description = "Ficha técnica completa e galeria de fotos do veículo")
record VehicleDetailResponse(
    @Schema(description = "ID único (UUID) do veículo", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID id,
    @Schema(description = "Slug amigável para URLs", example = "honda-civic-2022-exl")
    String slug,
    @Schema(description = "Título do anúncio", example = "Honda Civic 2.0 EXL Automático")
    String title,
    @Schema(description = "Marca do veículo", example = "Honda")
    String brand,
    @Schema(description = "Modelo do veículo", example = "Civic")
    String model,
    @Schema(description = "Versão específica", example = "2.0 16V EXL CVT")
    String version,
    @Schema(description = "Ano de fabricação", example = "2022")
    Integer year,
    @Schema(description = "Ano do modelo", example = "2022")
    Integer modelYear,
    @Schema(description = "Preço formatado em EUR (€)", example = "139.900,00 €")
    String price,
    @Schema(description = "Preço numérico exato", example = "139900.00")
    String rawPrice,
    @Schema(description = "Quilometragem rodada", example = "35000")
    Integer mileage,
    @Schema(description = "Tipo de transmissão", example = "AUTOMATIC")
    String transmission,
    @Schema(description = "Tipo de combustível", example = "FLEX")
    String fuelType,
    @Schema(description = "Cor do veículo", example = "Prata")
    String color,
    @Schema(description = "Quantidade de portas", example = "4")
    Integer doors,
    @Schema(description = "Descrição detalhada do veículo", example = "Veículo de único dono, revisões em dia na concessionária.")
    String description,
    @Schema(description = "Destaques e opcionais", example = "Bancos em couro, Teto solar, Painel digital")
    String highlights,
    @Schema(description = "Indica se está destacado", example = "true")
    boolean featured,
    @Schema(description = "Status atual do veículo", example = "PUBLISHED")
    String status,
    @Schema(description = "Galeria de imagens do veículo")
    List<ImageResponse> images
) {
}

@Schema(description = "Item resumido de veículo para tabela do painel administrativo")
record AdminVehicleResponse(
    @Schema(description = "ID único (UUID) do veículo", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID id,
    @Schema(description = "Slug amigável", example = "honda-civic-2022-exl")
    String slug,
    @Schema(description = "Título do veículo", example = "Honda Civic 2.0 EXL Automático")
    String title,
    @Schema(description = "Preço formatado em EUR (€)", example = "139.900,00 €")
    String price,
    @Schema(description = "Status do veículo", example = "PUBLISHED")
    String status,
    @Schema(description = "Indica se está destacado", example = "true")
    boolean featured,
    @Schema(description = "URL da imagem de capa", example = "/uploads/civic-cover.jpg")
    String coverImageUrl
) {
}

@Schema(description = "Imagem da galeria do veículo")
record ImageResponse(
    @Schema(description = "ID único (UUID) da foto", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID id,
    @Schema(description = "URL acessível para visualização", example = "/uploads/civic-1.jpg")
    String imageUrl,
    @Schema(description = "Posição de ordenação na galeria (0, 1, 2...)", example = "0")
    int sortOrder,
    @Schema(description = "Indica se esta foto é a capa principal", example = "true")
    boolean cover
) {
}

@Schema(description = "Formulário para cadastro ou atualização de veículo")
record VehicleFormRequest(
    @Schema(description = "Título descritivo do anúncio", example = "Honda Civic 2.0 EXL Automático", requiredMode = Schema.RequiredMode.REQUIRED)
    String title,
    @Schema(description = "Marca fabricante", example = "Honda", requiredMode = Schema.RequiredMode.REQUIRED)
    String brand,
    @Schema(description = "Modelo do veículo", example = "Civic", requiredMode = Schema.RequiredMode.REQUIRED)
    String model,
    @Schema(description = "Versão do veículo", example = "2.0 16V EXL CVT")
    String version,
    @Schema(description = "Ano de fabricação", example = "2022", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer year,
    @Schema(description = "Ano do modelo", example = "2022", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer modelYear,
    @Schema(description = "Preço de venda em euros", example = "139900.00", requiredMode = Schema.RequiredMode.REQUIRED)
    BigDecimal price,
    @Schema(description = "Quilometragem atual do veículo", example = "35000", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer mileage,
    @Schema(description = "Tipo de transmissão / câmbio", example = "AUTOMATIC", requiredMode = Schema.RequiredMode.REQUIRED)
    TransmissionType transmission,
    @Schema(description = "Tipo de combustível", example = "FLEX", requiredMode = Schema.RequiredMode.REQUIRED)
    FuelType fuelType,
    @Schema(description = "Cor do veículo", example = "Prata")
    String color,
    @Schema(description = "Quantidade de portas", example = "4")
    Integer doors,
    @Schema(description = "Descrição completa do veículo", example = "Veículo impecável, único dono, com todas as revisões feitas na concessionária autorizada.", requiredMode = Schema.RequiredMode.REQUIRED)
    String description,
    @Schema(description = "Destaques e opcionais", example = "Bancos em couro, Teto solar, Painel digital")
    String highlights,
    @Schema(description = "Marcar anúncio como destaque", example = "false")
    Boolean featured,
    @Schema(description = "Status inicial de publicação", example = "PUBLISHED")
    VehicleStatus status
) {
    VehicleUpsertRequest toCommand() {
        return new VehicleUpsertRequest(
            title,
            brand,
            model,
            version,
            year,
            modelYear,
            price,
            mileage,
            transmission,
            fuelType,
            color,
            doors,
            description,
            highlights,
            featured,
            status
        );
    }
}


final class VehicleMapper {
    private static final Locale PORTUGAL = Locale.forLanguageTag("pt-PT");

    private VehicleMapper() {
    }

    static VehicleCardResponse card(Vehicle vehicle) {
        return new VehicleCardResponse(
            vehicle.getId(),
            vehicle.getSlug(),
            vehicle.getTitle(),
            vehicle.getBrand(),
            vehicle.getModel(),
            vehicle.getYear(),
            vehicle.getModelYear(),
            vehicle.getMileage(),
            vehicle.getTransmission().name(),
            vehicle.getFuelType().name(),
            currency(vehicle.getPrice()),
            vehicle.getPrice().toPlainString(),
            coverUrl(vehicle),
            vehicle.isFeatured(),
            vehicle.getStatus().name(),
            vehicle.getHighlights()
        );
    }

    static VehicleDetailResponse detail(Vehicle vehicle) {
        List<ImageResponse> images = vehicle.getImages().stream()
            .sorted(Comparator.comparing(VehicleImage::getSortOrder))
            .map(VehicleMapper::image)
            .toList();
        return new VehicleDetailResponse(
            vehicle.getId(),
            vehicle.getSlug(),
            vehicle.getTitle(),
            vehicle.getBrand(),
            vehicle.getModel(),
            vehicle.getVersion(),
            vehicle.getYear(),
            vehicle.getModelYear(),
            currency(vehicle.getPrice()),
            vehicle.getPrice().toPlainString(),
            vehicle.getMileage(),
            vehicle.getTransmission().name(),
            vehicle.getFuelType().name(),
            vehicle.getColor(),
            vehicle.getDoors(),
            vehicle.getDescription(),
            vehicle.getHighlights(),
            vehicle.isFeatured(),
            vehicle.getStatus().name(),
            images
        );
    }

    static AdminVehicleResponse admin(Vehicle vehicle) {
        return new AdminVehicleResponse(
            vehicle.getId(),
            vehicle.getSlug(),
            vehicle.getTitle(),
            currency(vehicle.getPrice()),
            vehicle.getStatus().name(),
            vehicle.isFeatured(),
            coverUrl(vehicle)
        );
    }

    static ImageResponse image(VehicleImage vehicleImage) {
        return new ImageResponse(vehicleImage.getId(), vehicleImage.getImageUrl(), vehicleImage.getSortOrder(), vehicleImage.isCover());
    }

    private static String coverUrl(Vehicle vehicle) {
        return vehicle.getImages().stream()
            .filter(VehicleImage::isCover)
            .findFirst()
            .or(() -> vehicle.getImages().stream().findFirst())
            .map(VehicleImage::getImageUrl)
            .orElse("");
    }

    private static String currency(BigDecimal value) {
        if (value == null) {
            return "0,00 €";
        }
        return NumberFormat.getCurrencyInstance(PORTUGAL).format(value);
    }
}
