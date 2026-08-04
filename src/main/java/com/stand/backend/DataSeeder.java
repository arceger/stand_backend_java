package com.stand.backend;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
class DataSeeder implements CommandLineRunner {
    private final AdminUserRepository adminUserRepository;
    private final VehicleRepository vehicleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@stand.local}")
    private String defaultAdminEmail;

    @Value("${app.admin.password:Admin123!}")
    private String defaultAdminPassword;

    DataSeeder(
        AdminUserRepository adminUserRepository,
        VehicleRepository vehicleRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.adminUserRepository = adminUserRepository;
        this.vehicleRepository = vehicleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedAdmin();
        seedVehicles();
    }

    private void seedAdmin() {
        adminUserRepository.findByEmailIgnoreCase(defaultAdminEmail)
            .orElseGet(() -> adminUserRepository.save(
                new AdminUser(defaultAdminEmail, passwordEncoder.encode(defaultAdminPassword), "Administrador Stand")
            ));
    }

    private void seedVehicles() {
        if (vehicleRepository.count() > 0) {
            return;
        }

        vehicleRepository.save(buildVehicle(
            "Porsche Cayenne Coupe Platinum 2023",
            "Porsche",
            "Cayenne",
            "Coupe Platinum Edition",
            2023,
            2024,
            new BigDecimal("589900"),
            18500,
            TransmissionType.AUTOMATIC,
            FuelType.GASOLINE,
            "Cinza Vulcano",
            4,
            "SUV premium com acabamento impecavel, teto panoramico, pacote de assistencia e historico revisado.",
            "Teto panoramico, multimidia premium, bancos eletricos e pacote de assistencia",
            true,
            List.of(
                "https://coreva-normal.trae.ai/api/ide/v1/text_to_image?prompt=luxury%20graphite%20porsche%20cayenne%20coupe%20parked%20in%20a%20refined%20modern%20showroom%2C%20realistic%20automotive%20photography%2C%20high%20detail%2C%20editorial%20lighting&image_size=landscape_16_9",
                "https://coreva-normal.trae.ai/api/ide/v1/text_to_image?prompt=premium%20suv%20interior%20with%20tan%20leather%2C%20digital%20dashboard%2C%20luxury%20car%20dealership%20editorial%20photo%2C%20realistic&image_size=landscape_16_9",
                "https://coreva-normal.trae.ai/api/ide/v1/text_to_image?prompt=rear%20three%20quarter%20view%20of%20a%20graphite%20luxury%20suv%20inside%20minimal%20showroom%2C%20realistic%20high-end%20car%20photo&image_size=landscape_16_9"
            )
        ));

        vehicleRepository.save(buildVehicle(
            "Toyota Hilux SRX 4x4 2024",
            "Toyota",
            "Hilux",
            "SRX 4x4 AT",
            2024,
            2024,
            new BigDecimal("319900"),
            9400,
            TransmissionType.AUTOMATIC,
            FuelType.DIESEL,
            "Branco Perola",
            4,
            "Picape pronta para trabalho e lazer, com tracao 4x4, multimidia, camera 360 e excelente estado geral.",
            "4x4 diesel, camera 360, bancos em couro e revisoes em concessionaria",
            false,
            List.of(
                "https://coreva-normal.trae.ai/api/ide/v1/text_to_image?prompt=white%20toyota%20hilux%20pickup%20truck%20on%20a%20premium%20dealership%20platform%2C%20realistic%20studio%20lighting%2C%20high%20detail&image_size=landscape_16_9",
                "https://coreva-normal.trae.ai/api/ide/v1/text_to_image?prompt=modern%20pickup%20truck%20dashboard%20with%20leather%20seats%20and%20multimedia%20screen%2C%20realistic%20automotive%20interior%20photo&image_size=landscape_16_9",
                "https://coreva-normal.trae.ai/api/ide/v1/text_to_image?prompt=side%20view%20of%20a%20white%20premium%20pickup%20truck%20in%20industrial%20luxury%20garage%2C%20realistic%20photo&image_size=landscape_16_9"
            )
        ));

        vehicleRepository.save(buildVehicle(
            "BYD Dolphin Plus 2025",
            "BYD",
            "Dolphin",
            "Plus EV",
            2025,
            2025,
            new BigDecimal("184900"),
            3200,
            TransmissionType.AUTOMATIC,
            FuelType.ELECTRIC,
            "Azul Glacier",
            4,
            "Hatch eletrico moderno, silencioso e muito economico, com pacote ADAS, carregador portatil e acabamento premium.",
            "Eletrico, ADAS, camera 360 e autonomia urbana excelente",
            true,
            List.of(
                "https://coreva-normal.trae.ai/api/ide/v1/text_to_image?prompt=blue%20electric%20hatchback%20car%20displayed%20in%20futuristic%20showroom%2C%20realistic%20commercial%20automotive%20photo&image_size=landscape_16_9",
                "https://coreva-normal.trae.ai/api/ide/v1/text_to_image?prompt=clean%20electric%20car%20interior%20with%20rotating%20screen%20and%20bright%20premium%20materials%2C%20realistic%20photo&image_size=landscape_16_9",
                "https://coreva-normal.trae.ai/api/ide/v1/text_to_image?prompt=compact%20electric%20car%20rear%20view%20with%20sleek%20led%20lights%20in%20a%20minimal%20showroom%2C%20realistic&image_size=landscape_16_9"
            )
        ));
    }

    private Vehicle buildVehicle(
        String title,
        String brand,
        String model,
        String version,
        int year,
        int modelYear,
        BigDecimal price,
        int mileage,
        TransmissionType transmission,
        FuelType fuelType,
        String color,
        int doors,
        String description,
        String highlights,
        boolean featured,
        List<String> imageUrls
    ) {
        Vehicle vehicle = new Vehicle();
        vehicle.setTitle(title);
        vehicle.setBrand(brand);
        vehicle.setModel(model);
        vehicle.setVersion(version);
        vehicle.setYear(year);
        vehicle.setModelYear(modelYear);
        vehicle.setPrice(price);
        vehicle.setMileage(mileage);
        vehicle.setTransmission(transmission);
        vehicle.setFuelType(fuelType);
        vehicle.setColor(color);
        vehicle.setDoors(doors);
        vehicle.setDescription(description);
        vehicle.setHighlights(highlights);
        vehicle.setFeatured(featured);
        vehicle.setStatus(VehicleStatus.PUBLISHED);
        vehicle.setSlug(title.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", ""));

        for (int index = 0; index < imageUrls.size(); index++) {
            vehicle.getImages().add(new VehicleImage(vehicle, imageUrls.get(index), index, index == 0, "SEED", null));
        }
        return vehicle;
    }
}
