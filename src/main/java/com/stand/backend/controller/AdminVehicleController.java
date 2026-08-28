package com.stand.backend.controller;

import com.stand.backend.config.OpenApiConfig;
import com.stand.backend.dto.*;
import com.stand.backend.mapper.VehicleMapper;
import com.stand.backend.model.Vehicle;
import com.stand.backend.model.VehicleStatus;
import com.stand.backend.service.MarketplaceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Tag(name = "Painel Administrativo - Veículos", description = "Endpoints de gerenciamento do estoque e imagens")
@RestController
@RequestMapping("/api/admin/vehicles")
public class AdminVehicleController {

    private final MarketplaceService marketplaceService;

    public AdminVehicleController(MarketplaceService marketplaceService) {
        this.marketplaceService = marketplaceService;
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
    @GetMapping
    public AdminDashboardResponse listVehicles(
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
    @GetMapping("/{id}")
    public VehicleDetailResponse getVehicle(
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
    @PostMapping
    public ResponseEntity<VehicleDetailResponse> createVehicle(@RequestBody VehicleFormRequest request) {
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
    @PutMapping("/{id}")
    public VehicleDetailResponse updateVehicle(
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
    @PatchMapping("/{id}/status")
    public VehicleDetailResponse updateStatus(
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
    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<ImageResponse> uploadImages(
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
    @PutMapping(value = "/{id}/images/{imageId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<ImageResponse> replaceImage(
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
    @PutMapping("/{id}/images/order")
    public List<ImageResponse> reorderImages(
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
    @PatchMapping("/{id}/images/{imageId}/cover")
    public List<ImageResponse> setCover(
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
    @DeleteMapping("/{id}/images/{imageId}")
    public List<ImageResponse> deleteImage(
            @Parameter(description = "UUID do veículo", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID id,
            @Parameter(description = "UUID da imagem a ser removida", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID imageId
    ) {
        return marketplaceService.deleteImage(id, imageId).stream().map(VehicleMapper::image).toList();
    }
}
