package Baemin.News_Deliver.Domain.Mypage.Controller;

import Baemin.News_Deliver.Domain.Mypage.DTO.SettingDTO;
import Baemin.News_Deliver.Domain.Mypage.service.SettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/setting")
@RequiredArgsConstructor
public class SettingController {

    private final SettingService settingService;

    @GetMapping
    public ResponseEntity<List<SettingDTO>> getAllSetting(@RequestParam("userId") Long userId) {
        List<SettingDTO> settings = settingService.getAllSettingsByUserId(userId);
        return ResponseEntity.ok(settings);
    }

    /**



     */
    @PostMapping
    public ResponseEntity<Long> saveSetting(@RequestBody SettingDTO settingDTO) {

        return settingService.saveSetting(settingDTO);
    }

    @PutMapping
    public ResponseEntity<Void> updateSetting(@RequestBody SettingDTO settingDTO) {

        return settingService.updateSetting(settingDTO);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteSetting(@RequestParam("id") Long id) {

        return settingService.deleteSetting(id);
    }


}