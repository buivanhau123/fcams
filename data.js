/* ===== data.js: Quản lý dữ liệu mẫu, lịch tuần, lịch sử và phân quyền chặt chẽ (F-CAMS Simple) ===== */

const STORAGE_KEY = "fcams_simple_data_v2";
const SESSION_KEY = "fcams_simple_user_v2";

const SEED_USERS = [
  { username: "hungnq", password: "123", fullName: "Thầy Nguyễn Quốc Hùng", role: "LECTURER" },
  { username: "it_beta", password: "123", fullName: "Trần Văn Tuấn (Cán bộ IT)", role: "IT_STAFF" },
  { username: "admin", password: "123", fullName: "Quản trị viên Hệ thống", role: "ADMIN" }
];

const SEED_DATA = {
  rooms: [
    {
      id: "BE-301",
      building: "Beta",
      floor: 3,
      std: 35,
      cur: 30, // Thiếu 5 ghế
      assets: {
        projector: { name: "Máy chiếu Sony", status: "ACTIVE", note: "" },
        acLeft: { name: "Điều hòa Daikin (Trái)", status: "ACTIVE", note: "" },
        acRight: { name: "Điều hòa Daikin (Phải)", status: "ACTIVE", note: "" },
        tv: { name: "Smart TV Samsung 65 inch", status: "ACTIVE", note: "" }
      }
    },
    {
      id: "BE-302",
      building: "Beta",
      floor: 3,
      std: 35,
      cur: 40, // Mượn thêm 5 ghế từ BE-301
      assets: {
        projector: { name: "Máy chiếu Panasonic", status: "ACTIVE", note: "" },
        acLeft: { name: "Điều hòa Daikin (Trái)", status: "ACTIVE", note: "" },
        acRight: { name: "Điều hòa Daikin (Phải)", status: "ACTIVE", note: "" },
        tv: { name: "Smart TV LG 65 inch", status: "ACTIVE", note: "" }
      }
    },
    {
      id: "AL-101",
      building: "Alpha",
      floor: 1,
      std: 35,
      cur: 35,
      assets: {
        projector: { name: "Máy chiếu Epson", status: "FAULTY", note: "Bóng đèn chập chờn, mờ" },
        acLeft: { name: "Điều hòa Casper (Trái)", status: "ACTIVE", note: "" },
        acRight: { name: "Điều hòa Casper (Phải)", status: "ACTIVE", note: "" },
        tv: { name: "Smart TV Sony 75 inch", status: "ACTIVE", note: "" }
      }
    },
    {
      id: "AL-201",
      building: "Alpha",
      floor: 2,
      std: 35,
      cur: 35,
      assets: {
        projector: { name: "Máy chiếu Sony", status: "ACTIVE", note: "" },
        acLeft: { name: "Điều hòa Daikin (Trái)", status: "REPAIRING", note: "Đang nạp gas và vệ sinh lưới lọc" },
        acRight: { name: "Điều hòa Daikin (Phải)", status: "ACTIVE", note: "" },
        tv: { name: "Smart TV Sony 65 inch", status: "ACTIVE", note: "" }
      }
    },
    {
      id: "DE-101",
      building: "Delta",
      floor: 1,
      std: 40,
      cur: 40,
      assets: {
        projector: { name: "Máy chiếu Panasonic", status: "ACTIVE", note: "" },
        acLeft: { name: "Điều hòa Daikin (Trái)", status: "ACTIVE", note: "" },
        acRight: { name: "Điều hòa Daikin (Phải)", status: "ACTIVE", note: "" },
        tv: { name: "Smart TV Samsung 75 inch", status: "ACTIVE", note: "" }
      }
    },
    {
      id: "EP-201",
      building: "Epsilon",
      floor: 2,
      std: 30,
      cur: 30,
      assets: {
        projector: { name: "Máy chiếu Epson", status: "ACTIVE", note: "" },
        acLeft: { name: "Điều hòa Casper (Trái)", status: "ACTIVE", note: "" },
        acRight: { name: "Điều hòa Casper (Phải)", status: "ACTIVE", note: "" },
        tv: { name: "Smart TV LG 65 inch", status: "ACTIVE", note: "" }
      }
    },
    {
      id: "GA-301",
      building: "Gamma",
      floor: 3,
      std: 35,
      cur: 35,
      assets: {
        projector: { name: "Máy chiếu Sony", status: "ACTIVE", note: "" },
        acLeft: { name: "Điều hòa Daikin (Trái)", status: "ACTIVE", note: "" },
        acRight: { name: "Điều hòa Daikin (Phải)", status: "ACTIVE", note: "" },
        tv: { name: "Smart TV Samsung 65 inch", status: "ACTIVE", note: "" }
      }
    }
  ],
  transfers: [
    {
      id: "TR-01",
      from: "BE-301",
      to: "BE-302",
      qty: 5,
      reason: "Phòng BE-302 mượn ghế phục vụ thi cuối kỳ",
      by: "Trần Văn Tuấn (IT)",
      time: "08:30 hôm nay"
    }
  ],
  borrows: [
    {
      id: "BR-01",
      type: "ROOM",
      roomId: "BE-301",
      slot: "Slot 2 (09:10 - 10:40)",
      borrower: "hungnq",
      borrowerName: "Thầy Nguyễn Quốc Hùng",
      purpose: "Dạy bù môn SWP391",
      status: "BORROWED",
      createdAt: "08:45 hôm nay"
    },
    {
      id: "BR-02",
      type: "CHAIRS",
      roomId: "BE-302",
      fromRoom: "BE-301",
      qty: 5,
      slot: "Slot 3 (10:50 - 12:20)",
      borrower: "hungnq",
      borrowerName: "Thầy Nguyễn Quốc Hùng",
      purpose: "Bổ sung ghế cho lớp đông",
      status: "RETURN_REQUESTED", // Giảng viên đã nhấn Báo trả, chờ IT nghiệm thu
      createdAt: "10:15 hôm nay"
    }
  ],
  tickets: [
    {
      id: "TK-01",
      roomId: "AL-101",
      assetKey: "projector",
      assetName: "Máy chiếu",
      desc: "Bóng đèn máy chiếu chập chờn, mờ không nhìn rõ chữ",
      reporter: "Thầy Nguyễn Quốc Hùng",
      status: "OPEN",
      createdAt: "09:00 hôm nay"
    },
    {
      id: "TK-02",
      roomId: "AL-201",
      assetKey: "acLeft",
      assetName: "Điều hòa Daikin (Trái)",
      desc: "Dàn lạnh bị chảy nước xuống bàn sinh viên",
      reporter: "Trần Văn Tuấn (IT)",
      status: "IN_PROGRESS",
      createdAt: "Hôm qua"
    }
  ],

  // 1. DỮ LIỆU LỊCH 1 TUẦN (Thứ 2 đến Chủ nhật, Slot 1 đến Slot 6)
  schedule: [
    // BE-301
    { roomId: "BE-301", day: "Thứ 2", slot: 1, info: "Lớp PRJ301 (Thầy Hùng)" },
    { roomId: "BE-301", day: "Thứ 2", slot: 2, info: "Đang mượn dạy bù (Thầy Hùng)" },
    { roomId: "BE-301", day: "Thứ 3", slot: 3, info: "Lớp SWE201c (Cô Dung)" },
    { roomId: "BE-301", day: "Thứ 4", slot: 1, info: "Lớp PRJ301 (Thầy Hùng)" },
    { roomId: "BE-301", day: "Thứ 5", slot: 4, info: "Lớp IOT102 (Thầy Nam)" },
    { roomId: "BE-301", day: "Thứ 6", slot: 2, info: "Lớp CSD201 (Thầy Bình)" },

    // BE-302
    { roomId: "BE-302", day: "Thứ 2", slot: 3, info: "Lớp MAS291 (Thầy Minh)" },
    { roomId: "BE-302", day: "Thứ 2", slot: 4, info: "Lớp MAS291 (Thầy Minh)" },
    { roomId: "BE-302", day: "Thứ 4", slot: 2, info: "Thi EOS tập trung" },
    { roomId: "BE-302", day: "Thứ 6", slot: 5, info: "Sinh hoạt CLB IT" },

    // AL-101
    { roomId: "AL-101", day: "Thứ 2", slot: 1, info: "Lớp PRO192 (Thầy Tuấn)" },
    { roomId: "AL-101", day: "Thứ 3", slot: 2, info: "Lớp PRF192 (Cô Mai)" },
    { roomId: "AL-101", day: "Thứ 5", slot: 1, info: "Lớp PRO192 (Thầy Tuấn)" },

    // AL-201
    { roomId: "AL-201", day: "Thứ 3", slot: 4, info: "Lớp SSG104 (Thầy Đức)" },
    { roomId: "AL-201", day: "Thứ 5", slot: 2, info: "Lớp SSG104 (Thầy Đức)" },

    // DE-101
    { roomId: "DE-101", day: "Thứ 2", slot: 5, info: "Hội thảo đồ án Capstone" },
    { roomId: "DE-101", day: "Thứ 4", slot: 4, info: "Dự bị Tiếng Anh Level 5" },
    { roomId: "DE-101", day: "Thứ 6", slot: 1, info: "Dự bị Tiếng Anh Level 5" },

    // EP-201
    { roomId: "EP-201", day: "Thứ 3", slot: 1, info: "Studio Mỹ thuật số" },
    { roomId: "EP-201", day: "Thứ 5", slot: 3, info: "Studio Đồ họa 3D" },

    // GA-301
    { roomId: "GA-301", day: "Thứ 4", slot: 3, info: "Lớp Kỹ năng mềm" },
    { roomId: "GA-301", day: "Thứ 6", slot: 4, info: "Tập huấn Giảng viên mới" }
  ],

  // 2. LỊCH SỬ MƯỢN PHÒNG ĐÃ HOÀN TẤT (Cho IT & Admin)
  historyRooms: [
    {
      id: "BR-089",
      roomId: "BE-301",
      slot: "Slot 1 (07:30 - 09:00)",
      date: "16/10/2026",
      borrower: "Thầy Nguyễn Quốc Hùng",
      purpose: "Học bù lớp SE1801",
      returnTime: "09:05 16/10/2026",
      inspector: "Trần Văn Tuấn (IT)",
      acCheck: "Đã tắt 2 điều hòa",
      actualChairs: "35/35 ghế (Đủ)",
      status: "Đã hoàn tất nghiệm thu"
    },
    {
      id: "BR-085",
      roomId: "DE-101",
      slot: "Slot 4 (12:50 - 14:20)",
      date: "15/10/2026",
      borrower: "Cô Nguyễn Thùy Dung",
      purpose: "Họp nhóm môn SWP391",
      returnTime: "14:25 15/10/2026",
      inspector: "Trần Văn Tuấn (IT)",
      acCheck: "Đã tắt 2 điều hòa",
      actualChairs: "40/40 ghế (Đủ)",
      status: "Đã hoàn tất nghiệm thu"
    },
    {
      id: "BR-078",
      roomId: "AL-101",
      slot: "Slot 2 (09:10 - 10:40)",
      date: "14/10/2026",
      borrower: "Thầy Trần Đức Nam",
      purpose: "Báo cáo tiến độ lab",
      returnTime: "10:45 14/10/2026",
      inspector: "Trần Văn Tuấn (IT)",
      acCheck: "Đã tắt 2 điều hòa",
      actualChairs: "35/35 ghế (Đủ)",
      status: "Đã hoàn tất nghiệm thu"
    }
  ],

  // 3. LỊCH SỬ MƯỢN ĐỒ / MƯỢN GHẾ (Cho IT & Admin)
  historyItems: [
    {
      id: "EQ-101",
      itemName: "Bàn ghế sinh viên",
      qty: 5,
      fromRoom: "BE-301",
      toRoom: "BE-302",
      borrower: "Thầy Nguyễn Quốc Hùng",
      borrowDate: "15/10/2026",
      returnDate: "15/10/2026",
      purpose: "Bổ sung thi kết thúc môn",
      status: "Đã thu hồi hoàn trả đủ 5 ghế"
    },
    {
      id: "EQ-098",
      itemName: "Dây cáp HDMI 10m & Micro trợ giảng",
      qty: 1,
      fromRoom: "Kho IT Tòa Beta",
      toRoom: "BE-301",
      borrower: "Thầy Nguyễn Quốc Hùng",
      borrowDate: "14/10/2026",
      returnDate: "14/10/2026",
      purpose: "Dạy lớp thuyết trình chuyên đề",
      status: "Đã trả kho IT đầy đủ"
    },
    {
      id: "EQ-092",
      itemName: "Bàn ghế sinh viên",
      qty: 8,
      fromRoom: "AL-201",
      toRoom: "AL-101",
      borrower: "Thầy Trần Văn Tuấn (IT)",
      borrowDate: "12/10/2026",
      returnDate: "12/10/2026",
      purpose: "Lớp học ghép chuyên ngành",
      status: "Đã thu hồi hoàn trả đủ 8 ghế"
    }
  ],

  // 4. LỊCH SỬ SỬA CHỮA THIẾT BỊ (Cho IT & Admin)
  historyRepairs: [
    {
      id: "RP-054",
      roomId: "AL-101",
      assetName: "Máy chiếu Epson",
      issue: "Cháy bóng đèn cao áp do cắm điện đột ngột",
      technician: "Trần Văn Tuấn (IT)",
      startDate: "10/10/2026",
      finishDate: "11/10/2026",
      solution: "Thay bóng đèn chiếu mới và vệ sinh bộ tản nhiệt",
      status: "Đã sửa xong - Hoạt động tốt"
    },
    {
      id: "RP-048",
      roomId: "DE-101",
      assetName: "Điều hòa Daikin (Phải)",
      issue: "Chảy nước dàn lạnh, kêu to khi mở số lớn",
      technician: "Đội kỹ thuật Daikin Hòa Lạc",
      startDate: "08/10/2026",
      finishDate: "09/10/2026",
      solution: "Thông tắc đường ống thoát nước, bơm gas bổ sung",
      status: "Đã sửa xong - Hoạt động tốt"
    },
    {
      id: "RP-042",
      roomId: "BE-302",
      assetName: "Smart TV LG 65 inch",
      issue: "Mất kết nối cổng HDMI 1, màn hình giật sọc",
      technician: "Trần Văn Tuấn (IT)",
      startDate: "05/10/2026",
      finishDate: "05/10/2026",
      solution: "Thay thế bo mạch cổng HDMI, cập nhật lại firmware",
      status: "Đã sửa xong - Hoạt động tốt"
    }
  ]
};

function getState() {
  const raw = localStorage.getItem(STORAGE_KEY);
  if (!raw) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(SEED_DATA));
    return JSON.parse(JSON.stringify(SEED_DATA));
  }
  try {
    return JSON.parse(raw);
  } catch (e) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(SEED_DATA));
    return JSON.parse(JSON.stringify(SEED_DATA));
  }
}

function saveState(state) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
}

function resetData() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(SEED_DATA));
  alert("Đã đặt lại dữ liệu mẫu thành công!");
  location.reload();
}

function getUser() {
  try {
    return JSON.parse(sessionStorage.getItem(SESSION_KEY));
  } catch (e) {
    return null;
  }
}

function loginUser(username, password) {
  const u = SEED_USERS.find(x => x.username === username && x.password === password);
  if (u) {
    sessionStorage.setItem(SESSION_KEY, JSON.stringify(u));
    return u;
  }
  return null;
}

function logoutUser() {
  sessionStorage.removeItem(SESSION_KEY);
  location.href = "login.html";
}

// Kiểm tra quyền truy cập (chặn giảng viên vào trang của IT/Admin)
function checkPermission(allowedRoles) {
  const u = getUser();
  if (!u) {
    location.href = "login.html";
    return false;
  }
  if (!allowedRoles.includes(u.role)) {
    alert("Từ chối truy cập: Tài khoản Giảng viên không có quyền xem thông tin kỹ thuật này!");
    location.href = "schedule.html";
    return false;
  }
  return true;
}

// Render Menu phân quyền tuyệt đối:
// - Giảng viên: CHỈ hiển thị Lịch tuần & Phòng trống, Đơn mượn phòng/đồ, Phòng đang mượn & Báo trả
// - IT & Admin: Hiển thị đầy đủ Lịch tuần, Danh sách phòng, Bàn giao nghiệm thu, Lịch sử mượn & Sửa chữa, Báo cáo
function renderHeader(activeTab) {
  const u = getUser();
  if (!u) {
    location.href = "login.html";
    return;
  }

  const isLecturer = (u.role === "LECTURER");
  const roleName = u.role === "ADMIN" ? "Quản trị viên" : (u.role === "IT_STAFF" ? "Cán bộ IT" : "Giảng viên");
  
  let navItems = "";

  if (isLecturer) {
    // GIẢNG VIÊN: Chỉ xem lịch tuần, phòng trống, làm đơn mượn, báo trả phòng. Không xem thông tin không liên quan!
    navItems = `
      <a href="schedule.html" class="${activeTab === 'schedule' ? 'active' : ''}">Lịch tuần & Xem phòng trống</a> |
      <a href="borrow.html" class="${activeTab === 'borrow' ? 'active' : ''}">Làm đơn mượn phòng / mượn đồ</a> |
      <a href="borrow.html#list" class="${activeTab === 'my-borrows' ? 'active' : ''}">Phòng đang mượn & Báo trả</a> |
      <a href="javascript:void(0)" onclick="logoutUser()">Log out</a>
    `;
  } else {
    // CÁN BỘ IT VÀ QUẢN TRỊ VIÊN: Đầy đủ các phân hệ quản lý và lịch sử
    navItems = `
      <a href="schedule.html" class="${activeTab === 'schedule' ? 'active' : ''}">Lịch tuần & Xem phòng trống</a> |
      <a href="index.html" class="${activeTab === 'rooms' ? 'active' : ''}">Danh sách phòng & Thiết bị</a> |
      <a href="borrow.html" class="${activeTab === 'borrow' ? 'active' : ''}">Mượn phòng / đồ</a> |
      <a href="staff.html" class="${activeTab === 'staff' ? 'active' : ''}">Bàn giao & Nghiệm thu (IT)</a> |
      <a href="history.html" class="${activeTab === 'history' ? 'active' : ''}">Lịch sử mượn & Sửa chữa đồ</a> |
      <a href="report.html" class="${activeTab === 'report' ? 'active' : ''}">Báo cáo thống kê</a> |
      <a href="javascript:void(0)" onclick="logoutUser()">Log out</a>
    `;
  }

  const headerHtml = `
    <div class="header-bar">
      <div class="welcome-msg">Welcome, ${u.fullName} (${roleName})</div>
      <div class="nav-links">
        ${navItems}
      </div>
    </div>
  `;
  document.body.insertAdjacentHTML("afterbegin", headerHtml);

  // Thêm chân trang đơn giản
  const footerHtml = `
    <div class="footer-text">
      Hệ thống Quản lý Tài sản Phòng học (F-CAMS Simple) — Môn PRJ301 (Java Web)<br>
      <a href="javascript:void(0)" onclick="resetData()">[Đặt lại dữ liệu ban đầu]</a>
    </div>
  `;
  document.body.insertAdjacentHTML("beforeend", footerHtml);
}
